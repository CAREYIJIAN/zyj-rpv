package com.zyjclass.loadbalancer.impl;

import com.zyjclass.JrpcBootstrap;
import com.zyjclass.exceptions.LoadBalancerException;
import com.zyjclass.loadbalancer.AbstractLoadBalancer;
import com.zyjclass.loadbalancer.Selector;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于最短响应时间的负载均衡器
 * @author CAREYIJIAN$
 * @date 2024/1/25$
 */
@Slf4j
public class MinimumResponseTimeLoadBalancer extends AbstractLoadBalancer {

    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new MinimumResponseTimeSelector(serviceList);
    }

    private static class MinimumResponseTimeSelector implements Selector{
        public MinimumResponseTimeSelector(List<InetSocketAddress> serviceList){

        }

        @Override
        public InetSocketAddress getNext() {
            Map.Entry<Long, Channel> entry = JrpcBootstrap.ANSWER_TIME_CHANNEL_MAP.firstEntry();
            if (entry != null){
                if (log.isDebugEnabled()){
                    log.debug("选取了响应时间为【{}】ms的服务节点",entry.getKey());
                }
                return (InetSocketAddress) entry.getValue().remoteAddress();
            }else {
                //直接从缓存中获取一个可用的就行了
                Channel channel = (Channel)JrpcBootstrap.CHANNEL_MAP.values().toArray()[0];
                return (InetSocketAddress)channel.remoteAddress();
            }

        }

        @Override
        public void reBalance() {

        }
    }

}
