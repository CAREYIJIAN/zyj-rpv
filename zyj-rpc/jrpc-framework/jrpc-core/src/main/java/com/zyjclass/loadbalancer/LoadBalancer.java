package com.zyjclass.loadbalancer;

import java.net.InetSocketAddress;

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
    InetSocketAddress selectServiceAddress(String sviceName);
}
