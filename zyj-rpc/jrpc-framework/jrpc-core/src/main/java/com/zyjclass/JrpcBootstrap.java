package com.zyjclass;

import com.zyjclass.annotation.JrpcApi;
import com.zyjclass.channelhandler.handler.JrpcRequestDecoder;
import com.zyjclass.channelhandler.handler.JrpcResponseEncoder;
import com.zyjclass.channelhandler.handler.MethodCallHandler;
import com.zyjclass.core.HeartbeatDetector;
import com.zyjclass.discovery.Registry;
import com.zyjclass.discovery.RegistryConfig;
import com.zyjclass.loadbalancer.LoadBalancer;
import com.zyjclass.loadbalancer.impl.ConsistentHashBalancer;
import com.zyjclass.loadbalancer.impl.MinimumResponseTimeLoadBalancer;
import com.zyjclass.loadbalancer.impl.RoundRobinLoadBalancer;
import com.zyjclass.transport.message.JrpcRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.server.Request;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * @author CAREYIJIAN$
 * @date 2024/1/17$
 */
//lombok可以根据你加的相关注解，在编译的时候增强你的代码，如果没有引入lombok加日志要这么写
//private static final Logger logger = LoggerFactory.getLogger(JrpcBootstrap.class);
@Slf4j
public class JrpcBootstrap {
    //JrpcBootstrap是个单例，每个应用程序只有一个实例
    private static final JrpcBootstrap jrpcBootstrap = new JrpcBootstrap();
    //全局的配置中心
    private Configuration configuration;
    //保存request对象，可以到当前线程中随时获取
    public static final ThreadLocal<JrpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();
    //维护已经发布且暴露的服务列表 key->interface的全限定名 value-> ServiceConfig<?>
    public static final Map<String, ServiceConfig<?>> SERVICE_MAP = new ConcurrentHashMap<>(16);

    //连接的缓存,ps:如果使用InetSocketAddress这样的类做key，一定要看他有没有重写equals方法和toString
    public static final Map<InetSocketAddress, Channel> CHANNEL_MAP = new ConcurrentHashMap<>(16);
    //存储响应时间和对应的通道
    public static final TreeMap<Long, Channel> ANSWER_TIME_CHANNEL_MAP = new TreeMap<>();

    //定义全局对外挂起的completableFuture
    public final static Map<Long, CompletableFuture<Object>> PENDING_MAP = new ConcurrentHashMap<>(128);

    /*-----------------------------------通用核心api-------------------------------------*/
    private JrpcBootstrap(){
        //构造启动引导程序，时要做一些初始化的事情。
        configuration = new Configuration();
    }
    public static JrpcBootstrap getInstance() {
        return jrpcBootstrap;
    }

    /**
     * 用来定义当前应用的名字
     * @param appName
     * @return this（当前实例）
     */
    public JrpcBootstrap application(String appName) {
        configuration.setAppName(appName);
        return this;
    }

    /**
     * 用来配置一个注册中心
     * @param registryConfig（注册中心）
     * @return this（当前实例）
     */
    public JrpcBootstrap registry(RegistryConfig registryConfig) {
        /*这里维护一个zookeeper实例，但是这样就会将zookeeper和当前工程耦合,我们希望是可以扩展更多种不同的实现
        zooKeeper = ZookeeperUtil.createZookeeper();*/
        //可以尝试使用registryConfig获取一个注册中心，有点工厂设计模式的意思了
        configuration.setRegistryConfig(registryConfig);
        return this;
    }
    /**
     * 用来配置一个负载均衡策略
     * @param loadBalancer（负载均衡器）
     * @return this（当前实例）
     */
    public JrpcBootstrap loadBalancer(LoadBalancer loadBalancer) {
        configuration.setLoadBalancer(loadBalancer);
        return this;
    }

    /**
     * 用来配置序列化协议
     * @param protocalConfig（序列化协议的封装）
     * @return this（当前实例）
     */
    public JrpcBootstrap protocol(ProtocolConfig protocalConfig) {
        configuration.setProtocolConfig(protocalConfig);
        if (log.isDebugEnabled()){
            log.debug("当前工程使用了：{}协议进行序列化",protocalConfig.toString());
        }
        return this;
    }

    /*-----------------------------------服务提供方相关api-------------------------------------*/
    /**
     * 服务的发布 （核心：将接口和实现注册到服务中心）
     * @param service （封装需要发布的服务）
     * @return this（当前实例）
     */
    public JrpcBootstrap publish(ServiceConfig<?> service) {
        //我们抽象了注册中心的概念，使用注册中心的一个实现完成注册
        configuration.getRegistryConfig().getRegistry().register(service);

        //当服务调用方调用接口、方法名、具体的方法参数列表发起调用，如何提供和选择实现？
        //方案：1.new一个 2.spring beanFactory.getBean(Class) 3.自己维护映射关系
        SERVICE_MAP.put(service.getInterface().getName(),service);
        return this;
    }

    /**
     * 批量服务的发布
     * @param services （封装需要发布的服务集合）
     * @return this（当前实例）
     */
    public JrpcBootstrap publish(List<ServiceConfig<?>> services) {
        for (ServiceConfig<?> service : services){
            this.publish(service);
        }
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {
        //创建eventloop，老板只负责处理请求，之后会将请求分发至worker
        EventLoopGroup boss = new NioEventLoopGroup(2);
        EventLoopGroup worker = new NioEventLoopGroup(10);
        try {
            //服务器引导程序
            ServerBootstrap b = new ServerBootstrap();//用于启动NIO服务
            //配置服务器
            b.group(boss,worker)
                    .channel(NioServerSocketChannel.class)//通过工厂方法设计模式实例化一个channel
                    .localAddress(new InetSocketAddress(configuration.getPort()))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //核心在这里，我们需要加入很多入站和出站的handler
                            socketChannel.pipeline()
                                    //日志处理
                                    .addLast(new LoggingHandler())
                                    //解码器
                                    .addLast(new JrpcRequestDecoder())
                                    //根据请求进行方法调用
                                    .addLast(new MethodCallHandler())
                                    //响应的编码
                                    .addLast(new JrpcResponseEncoder());
                        }
                    });
            //绑定服务器，该实例将提供有关IO操作的结果或状态信息
            ChannelFuture channelFuture = b.bind().sync();
            channelFuture.channel().closeFuture().sync();
        }catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            try {
                boss.shutdownGracefully().sync();
                worker.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /*-----------------------------------服务调用方相关api-------------------------------------*/
    public JrpcBootstrap reference(ReferenceConfig<?> reference) {
        //开启对这个服务的心跳检测
        HeartbeatDetector.detectHeartbeat(reference.getInterface().getName());

        //此方法中要拿到相关配置项-注册中心
        //配置reference，将来调用get方法时生成代理对象
        //1.reference需要一个注册中心
        reference.setRegistry(configuration.getRegistryConfig().getRegistry());

        return this;
    }


    /**
     * 配置序列化的方式
     * @param serializeType 序列化的方式
     * @return
     */
    public JrpcBootstrap serialize(String serializeType) {
        configuration.setSerializeType(serializeType);
        if (log.isDebugEnabled()){
            log.debug("配置了序列化的方式为【{}】",serializeType);
        }
        return this;
    }

    /**
     * 配置压缩的算法
     * @param compressType 压缩的方式
     * @return
     */
    public JrpcBootstrap compress(String compressType) {
        configuration.setCompressType(compressType);
        if (log.isDebugEnabled()){
            log.debug("配置了压缩的算法为【{}】",compressType);
        }
        return this;
    }

    /**
     * 扫描包，进行批量注册
     * @param packageName 包名
     * @return
     */
    public JrpcBootstrap scan(String packageName) {
        //需要通过packageName获取其下的所有的类的权限定名称
        List<String> classNames = getAllClassNames(packageName);
        //通过反射获取他的接口，构建具体的实现
        List<Class<?>> classes = classNames.stream().map(className -> {
                    try {
                        return Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }).filter(clazz -> clazz.getAnnotation(JrpcApi.class) != null)
                .collect(Collectors.toList());

        for (Class<?> clazz : classes) {
            //获取他的接口
            Class<?>[] interfaces = clazz.getInterfaces();
            Object instance = null;
            try {
                instance = clazz.getConstructor().newInstance();
            }catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            for (Class<?> anInterface : interfaces) {
                ServiceConfig<?> serviceConfig = new ServiceConfig<>();
                serviceConfig.setInterface(anInterface);
                serviceConfig.setRef(instance);
                if (log.isDebugEnabled()){
                    log.debug("---->已经通过包扫描，将服务【{}】发布",anInterface);
                }
                //进行发布
                publish(serviceConfig);
            }
        }
        return this;
    }

    private List<String> getAllClassNames(String packageName) {
        //1.通过packageName获得绝对路径
        //com.zyjclass.xxx.yyy -> E://xxx/xxx/xxx/com/zyjclass/xxx/yyy
        String basePath = packageName.replaceAll("\\.", "/");
        URL url = ClassLoader.getSystemClassLoader().getResource(basePath);
        if (url == null){
            throw new RuntimeException("包扫描时，发现路径不存在");
        }
        String absolutePath = url.getPath();
        List<String> classNames = new ArrayList<>();
        classNames = recursionFile(absolutePath,classNames,basePath);
        return classNames;
    }

    private List<String> recursionFile(String absolutePath, List<String> classNames, String basePath) {
        //获取文件
        File file = new File(absolutePath);
        //判断文件是否是文件夹
        if (file.isDirectory()){
            //找到文件夹的所有文件
            File[] children = file.listFiles(pathname -> pathname.isDirectory() || pathname.getPath().contains(".class"));
            if (children == null || children.length == 0){
                return classNames;
            }
            for (File child : children) {
                if (child.isDirectory()){
                    //递归调用
                    recursionFile(child.getAbsolutePath(),classNames,basePath);
                }else {
                    //文件 --》 类的权限定名称
                    String className = getClassNameByAbsolutePath(child.getAbsolutePath(),basePath);
                    classNames.add(className);
                }
            }
        }else {
            //文件 --》 类的权限定名称
            String className = getClassNameByAbsolutePath(absolutePath,basePath);
            classNames.add(className);
        }
        return classNames;
    }

    private String getClassNameByAbsolutePath(String absolutePath, String basePath) {
        //E:\project\zyjclass-jrpc\.....\com\zyjclass\serialize\xxx.class  ---->  com.zyjclass.serialize.xxx

        String fileName = absolutePath
                .substring(absolutePath.indexOf(basePath.replaceAll("/","\\\\")))
                .replaceAll("\\\\",".");

        fileName = fileName.substring(0,fileName.indexOf(".class"));
        return fileName;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
