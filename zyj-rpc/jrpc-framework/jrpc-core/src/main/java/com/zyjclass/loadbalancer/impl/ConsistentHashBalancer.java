package com.zyjclass.loadbalancer.impl;

import com.zyjclass.JrpcBootstrap;
import com.zyjclass.exceptions.LoadBalancerException;
import com.zyjclass.loadbalancer.AbstractLoadBalancer;
import com.zyjclass.loadbalancer.Selector;
import com.zyjclass.transport.message.JrpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 一致性hash的负载均衡策略
 * @author CAREYIJIAN$
 * @date 2024/1/24$
 */
@Slf4j
public class ConsistentHashBalancer extends AbstractLoadBalancer {

    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new ConsistentHashSelector(serviceList,128);
    }

    /**
     * 一致性hash算法的具体实现
     */
    private static class ConsistentHashSelector implements Selector{

        //hash环来存储服务器节点
        private SortedMap<Integer, InetSocketAddress> circle = new TreeMap<>();
        //虚拟节点的个数(为了解决，由于服务节点过于少，而导致有可能会出现，两个服务节点的hash值太接近，负载不均衡的状况。)
        private int virtualNodes;
        private List<InetSocketAddress> serviceList;
        public ConsistentHashSelector(List<InetSocketAddress> serviceList, int virtualNodes){
            //我们应该尝试将节点转化为虚拟节点，进行挂载
            this.virtualNodes = virtualNodes;
            for (InetSocketAddress inetSocketAddress : serviceList){
                addNodeToCircle(inetSocketAddress);
            }
        }

        @Override
        public InetSocketAddress getNext() {
            //1.hash环已经建立，下面要对请求的要素做处理，应该选择什么要素来进行hash运算
            //有没有办法可以获取，到具体的请求内容（jrpcRequest）--> ThreadLocal
            JrpcRequest jrpcRequest = JrpcBootstrap.REQUEST_THREAD_LOCAL.get();

            //根据请求的一些特征来选择服务器 id
            String requestId = Long.toString(jrpcRequest.getRequestId());

            //请求的id做hash，字符串默认的hash不太好(若产生连续的id，生成的hash也是连续的不够散)
            int hash = hash(requestId);

            //判断该hash值是否能直接落在一个服务器上，和服务器的hash一样
            if (!circle.containsKey(hash)){
                //寻找离我最近的节点
                SortedMap<Integer, InetSocketAddress> tailMap = circle.tailMap(hash);
                //没有就取最小的
                hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
            }

            return circle.get(hash);
        }

        /**
         * 将每个节点挂载到hash环上
         * @param inetSocketAddress 节点的地址
         */
        private void addNodeToCircle(InetSocketAddress inetSocketAddress) {
            //为每一个节点生成匹配的虚拟节点进行挂载
            for (int i = 0; i < virtualNodes; i++){
                int hash = hash(inetSocketAddress.toString() + "-" + i);
                //挂载到hash环上
                circle.put(hash,inetSocketAddress);
                if (log.isDebugEnabled()){
                    log.debug("hash为【{}】的节点已经挂载到哈希环上",hash);
                }
            }
        }

        /**
         * 删除节点
         * @param inetSocketAddress
         */
        private void removeNodeFromCircle(InetSocketAddress inetSocketAddress) {
            for (int i = 0; i < virtualNodes; i++){
                int hash = hash(inetSocketAddress.toString() + "-" + i);
                circle.remove(hash);
            }
        }
        /**
         * 具体的hash算法(基于md5)
         * @param s
         * @return
         */
        private int hash(String s) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
            }catch (NoSuchAlgorithmException e){
                throw new RuntimeException(e);
            }
            //md5得到的结果是一个字节数组，但是我们需要int 4个字节
            byte[] digest = md.digest(s.getBytes());

            int res = 0;
            for (int i = 0; i < 4; i++){
                res = res << 8;
                if (digest[i] < 0){
                    res = res | (digest[i] & 255);
                }else {
                    res =  res | digest[i];
                }
            }
            return res;
        }

    }
}
