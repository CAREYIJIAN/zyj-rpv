package com.zyjclass.proxy;

import com.zyjclass.JrpcBootstrap;
import com.zyjclass.ReferenceConfig;
import com.zyjclass.discovery.RegistryConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author CAREYIJIAN$
 * @date 2024/2/1$
 */
public class ProxyFactory {

    private static Map<Class<?>,Object> cache = new ConcurrentHashMap<>(32);

    public static <T> T getProxy(Class<T> clazz){

        Object bean = cache.get(clazz);
        if (bean != null){
            return (T)bean;
        }

        //服务的调用方，要想尽一切办法获取代理对象，使用ReferenceConfig进行封装
        ReferenceConfig<T> reference = new ReferenceConfig<>();
        reference.setInterface(clazz);
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
                .group("primary")
                .reference(reference); //要调用的接口定义
        //获取代理对象
        T t = reference.get();
        cache.put(clazz,t);
        return t;
    }

}
