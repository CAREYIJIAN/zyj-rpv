package com.zyjclass;

import com.zyjclass.channelHandler.handler.JrpcRequestDecoder;
import com.zyjclass.channelHandler.handler.JrpcResponseEncoder;
import com.zyjclass.channelHandler.handler.MethodCallHandler;
import com.zyjclass.discovery.Registry;
import com.zyjclass.discovery.RegistryConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author CAREYIJIAN$
 * @date 2024/1/17$
 */
@Slf4j
public class JrpcBootstrap {
    //lombok可以根据你加的相关注解，在编译的时候增强你的代码，如果没有引入lombok加日志要这么写
    //private static final Logger logger = LoggerFactory.getLogger(JrpcBootstrap.class);


    /*-----------------------------------通用核心api-------------------------------------*/
    //JrpcBootstrap是个单例，每个应用程序只有一个实例
    private static final JrpcBootstrap jrpcBootstrap = new JrpcBootstrap();
    //定义相关的基础配置
    private String appName = "default";
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    public static final IdGenerator ID_GENERATOR = new IdGenerator(1,2);
    //private ZooKeeper zooKeeper; //维护一个zookeeper实例
    private int port = 8088;
    private Registry registry;
    //维护已经发布且暴露的服务列表 key->interface的全限定名 value-> ServiceConfig<?>
    public static final Map<String, ServiceConfig<?>> SERVICE_MAP = new ConcurrentHashMap<>(16);

    //连接的缓存,ps:如果使用InetSocketAddress这样的类做key，一定要看他有没有重写equals方法和toString
    public static final Map<InetSocketAddress, Channel> CHANNEL_MAP = new ConcurrentHashMap<>(16);
    //定义全局对外挂起的completableFuture
    public final static Map<Long, CompletableFuture<Object>> PENDING_MAP = new ConcurrentHashMap<>(128);

    private JrpcBootstrap(){
        //构造启动引导程序，时要做一些初始化的事情。

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
        this.appName = appName;
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

        //尝试使用registryConfig获取一个注册中心，有点工厂设计模式的意思了
        this.registry = registryConfig.getRegistry();
        return this;
    }

    /**
     * 用来配置序列化协议
     * @param protocalConfig（序列化协议的封装）
     * @return this（当前实例）
     */
    public JrpcBootstrap protocol(ProtocolConfig protocalConfig) {
        this.protocolConfig = protocalConfig;
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
        registry.register(service);

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
                    .localAddress(new InetSocketAddress(port))
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
            System.out.println();
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
        //此方法中要拿到相关配置项-注册中心
        //配置reference，将来调用get方法时生成代理对象
        //1.reference需要一个注册中心
        reference.setRegistry(registry);

        return this;
    }







}
