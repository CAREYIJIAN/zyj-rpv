package com.zyjclass;

import com.zyjclass.discovery.Registry;
import com.zyjclass.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

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
        Class[] classes  = new Class[]{interfaceRef};
        //使用动态代理生成代理对象
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //我们调用syHi事实上会走进这个代码段中，我们已经知道method，args
                log.info("method-->{}",method.getName());
                log.info("args-->{}",args);

                //1.发现服务，从注册中心寻找一个可用发服务
                //传入服务的名字,返回一个ip+端口
                InetSocketAddress address = registry.lookUp(interfaceRef.getName());
                if (log.isDebugEnabled()){
                    log.debug("服务调用方发现了服务【{}】的可用主机【{}】", interfaceRef, address);
                }
                //2.使用netty连接服务器，发送调用的服务的名字+方法名+参数列表，返回结果

                System.out.println("hello proxy");
                return null;
            }
        });
        return (T)helloProxy;
    }

    public void setRegistry(Registry registry) {
    }

    public Registry getRegistry() {
        return registry;
    }
}
