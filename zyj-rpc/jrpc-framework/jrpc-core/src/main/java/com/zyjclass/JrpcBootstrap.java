package com.zyjclass;

import lombok.extern.slf4j.Slf4j;

import java.util.List;


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
    private static JrpcBootstrap jrpcBootstrap = new JrpcBootstrap();

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

        return this;
    }

    /**
     * 用来配置一个注册中心
     * @param registryConfig（注册中心）
     * @return this（当前实例）
     */
    public JrpcBootstrap registry(RegistryConfig registryConfig) {
        return this;
    }

    /**
     * 用来配置序列化协议
     * @param protocalConfig（序列化协议的封装）
     * @return this（当前实例）
     */
    public JrpcBootstrap protocol(ProtocolConfig protocalConfig) {
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
        if (log.isDebugEnabled()){
            log.debug("服务{}，已经被注册",service.getInterface().getName());
        }
        return this;
    }

    /**
     * 批量服务的发布
     * @param services （封装需要发布的服务集合）
     * @return this（当前实例）
     */
    public JrpcBootstrap publish(List<ServiceConfig<?>> services) {
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {
    }


    /*-----------------------------------服务调用方相关api-------------------------------------*/
    public JrpcBootstrap reference(ReferenceConfig<?> reference) {
        //此方法中要拿到相关配置项-注册中心
        //配置reference，将来调用get方法时生成代理对象

        return this;
    }







}
