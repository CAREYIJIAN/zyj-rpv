package com.zyjclass.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 负载均衡器的接口
 * @author CAREYIJIAN$
 * @date 2024/1/24$
 */
public interface LoadBalancer {
    /**
     * 根据服务名找到一个可用的服务
     * @param sviceName 服务名
     * @return  可用的服务地址
     */
    InetSocketAddress selectServiceAddress(String sviceName, String group);

    /**
     * 当感知节点发生了动态上下线，需要重新进行负载均衡
     * @param serviceName 服务名称
     * @param addresses 新的服务列表
     */
    void reLoadBalance(String serviceName, List<InetSocketAddress> addresses);
}
