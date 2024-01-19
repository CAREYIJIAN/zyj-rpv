package com.zyjclass;

import com.zyjclass.discovery.Registry;
import com.zyjclass.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    //private ZooKeeper zooKeeper; //维护一个zookeeper实例
    private int port = 8088;
    private Registry registry;
    //维护已经发布且暴露的服务列表 key->interface的全限定名 value-> ServiceConfig<?>
    private static final Map<String, ServiceConfig<?>> SERVICE_MAP = new ConcurrentHashMap<>(16);

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
        try {
            Thread.sleep(100000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
