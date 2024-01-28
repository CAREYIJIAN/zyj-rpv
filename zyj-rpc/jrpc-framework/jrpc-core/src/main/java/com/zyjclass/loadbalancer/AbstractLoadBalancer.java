package com.zyjclass.loadbalancer;

import com.zyjclass.JrpcBootstrap;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/24$
 */
public abstract class AbstractLoadBalancer implements LoadBalancer{

    //一个服务会匹配一个selector，而不是匹配一个LoadBalancer
    private Map<String, Selector> cache = new ConcurrentHashMap<>(8);
    public AbstractLoadBalancer(){
    }
    @Override
    public InetSocketAddress selectServiceAddress(String sviceName) {
        //1.优先从cache中获取一个选择器
        Selector selector = cache.get(sviceName);

        //2.如果没有，就需要为这个service创建一个selector
        if (selector == null){
            //负载均衡器内部应该维护一个服务列表作为缓存
            List<InetSocketAddress> serviceList = JrpcBootstrap.getInstance().getConfiguration().getRegistryConfig().getRegistry().lookUp(sviceName);

            //提供一些算法负载选取合适的节点
            selector = getSelector(serviceList);

            //将selector放入缓存
            cache.put(sviceName,selector);
        }
        return selector.getNext();
    }

    /**
     * 由子类进行扩展
     * @param serviceList 服务列表
     * @return 负载均衡算法选择器
     */
    protected abstract Selector getSelector(List<InetSocketAddress> serviceList);

    @Override
    public synchronized void reLoadBalance(String serviceName, List<InetSocketAddress> addresses){
        //我们可根据新的服务列表生成新的selector
        cache.put(serviceName,getSelector(addresses));
    }

}
