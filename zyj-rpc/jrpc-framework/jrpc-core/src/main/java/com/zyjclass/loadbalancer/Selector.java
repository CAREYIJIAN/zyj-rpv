package com.zyjclass.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/24$
 */
public interface Selector {

    /**
     * 根据服务列表执行一种算法获取一个服务节点
     * @return 具体的服务节点
     */
    InetSocketAddress getNext();

}
