package com.zyjclass.loadbalancer.impl;

import com.zyjclass.exceptions.LoadBalancerException;
import com.zyjclass.loadbalancer.AbstractLoadBalancer;
import com.zyjclass.loadbalancer.Selector;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询的负载均衡策略
 * @author CAREYIJIAN$
 * @date 2024/1/24$
 */
@Slf4j
public class RoundRobinLoadBalancer extends AbstractLoadBalancer {

    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new RoundRobinSelector(serviceList);
    }

    private static class RoundRobinSelector implements Selector{

        private List<InetSocketAddress> serviceList;
        private AtomicInteger index; //游标
        public RoundRobinSelector(List<InetSocketAddress> serviceList){
            this.serviceList = serviceList;
            this.index = new AtomicInteger(0);
        }

        @Override
        public InetSocketAddress getNext() {
            if (serviceList == null || serviceList.size() == 0){
                log.error("进行负载均衡选取节点时发现服务列表为空");
                throw new LoadBalancerException();
            }

            InetSocketAddress address = serviceList.get(index.get());

            //如果到了最后的位置，重置游标
            if (index.get() == serviceList.size() - 1){
                index.set(0);
            }else {
                //游标后移
                index.incrementAndGet();
            }
            return address;
        }
    }
}
