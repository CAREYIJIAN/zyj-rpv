package com.zyjclass;

import com.zyjclass.discovery.Registry;
import com.zyjclass.exceptions.NetworkException;
import com.zyjclass.proxy.handler.RpcConsumerInvocationHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/17$
 */
@Slf4j
public class ReferenceConfig<T> {
    private Class<T> interfaceRef;
    private Registry registry;

    public Class<T> getInterface() {
        return interfaceRef;
    }

    public void setInterface(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    /**
     * 代理设计模式，生成一个api接口的代理对象
     * @return
     */
    public T get() {
        //此处使用动态代理完成一些工作
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<T>[] classes  = new Class[]{interfaceRef};
        //使用动态代理生成代理对象
        InvocationHandler handler = new RpcConsumerInvocationHandler(registry,interfaceRef);
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, handler);
        return (T)helloProxy;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public Registry getRegistry() {
        return registry;
    }
}
