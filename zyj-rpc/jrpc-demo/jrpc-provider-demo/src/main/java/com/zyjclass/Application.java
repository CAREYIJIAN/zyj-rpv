package com.zyjclass;

import com.zyjclass.impl.HelloJrpcImpl;

import java.security.Provider;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/17$
 */
public class Application {
    public static void main(String[] args) {
        //服务提供方，需要注册服务，启动服务
        /**
         * 1.封装要发布的服务
         * 2.定义注册中心
         * 3.通过启动引导程序，启动服务提供方
         *   1）配置（应用程序名、注册中心、序列化协议、压缩方式）
         *   2）发布服务
         */
        //封装要发布的服务
        ServiceConfig<HelloJrpc> service = new ServiceConfig<>();
        service.setInterface(HelloJrpc.class);
        service.setRef(new HelloJrpcImpl());

        //通过启动引导程序，启动服务提供方
        JrpcBootstrap.getInstance()
                .application("jrpc-provider") //名称
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))  //配置注册中心
                .protocol(new ProtocolConfig("jdk"))  //配置协议
                .publish(service) //发布服务
                .start();  //启动服务

    }
}
