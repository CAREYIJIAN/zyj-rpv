package com.zyjclass;

import com.zyjclass.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/17$
 */
@Slf4j
public class ConsumerApplication {
    public static void main(String[] args) {
        //服务的调用方，要想尽一切办法获取代理对象，使用ReferenceConfig进行封装
        ReferenceConfig<HelloJrpc> reference = new ReferenceConfig<>();
        reference.setInterface(HelloJrpc.class);
        /**
         * 代理做那些事情
         * 1、连接注册中心
         * 2、拉取服务列表
         * 3、选择一个服务并建立连接
         * 4、发送请求，携带一些信息（接口名，参数列表，方法名）
         */
        JrpcBootstrap.getInstance()
                .application("jrpc-consumer") //名称
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))  //配置注册中心
                .serialize("hessian")
                .compress("gzip")
                .reference(reference); //要调用的接口定义
        //获取代理对象
        HelloJrpc helloJrpc = reference.get();
        String hi = helloJrpc.sayHi("Hi");
        log.info("syHi-->{}",hi);

    }
}
