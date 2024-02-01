package com.zyjclass;

import com.zyjclass.discovery.RegistryConfig;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/31$
 */
@Component
public class JrpcStarter implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        Thread.sleep(5000);
        //log.info("--------------------<<==========jrpc开始启动==========>>----------------------");
        //通过启动引导程序，启动服务提供方
        JrpcBootstrap.getInstance()
                .application("jrpc-provider") //名称
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))  //配置注册中心
                .serialize("jdk")
                //.publish(service) //发布服务
                .scan("com.zyjclass.impl")//扫包批量发布
                .start();  //启动服务
    }

}
