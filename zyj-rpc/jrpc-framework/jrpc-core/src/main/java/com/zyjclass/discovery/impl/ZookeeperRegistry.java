package com.zyjclass.discovery.impl;

import com.zyjclass.Constant;
import com.zyjclass.ServiceConfig;
import com.zyjclass.discovery.AbstractRegistry;
import com.zyjclass.exceptions.DiscoveryException;
import com.zyjclass.exceptions.NetworkException;
import com.zyjclass.utils.NetUtils;
import com.zyjclass.utils.zookeeper.ZookeeperNode;
import com.zyjclass.utils.zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/18$
 */
@Slf4j
public class ZookeeperRegistry extends AbstractRegistry {
    //维护一个zookeeper实例
    private ZooKeeper zooKeeper;
    public ZookeeperRegistry(){
        this.zooKeeper = ZookeeperUtil.createZookeeper();
    }
    public ZookeeperRegistry(String connectSting, int timeout){
        this.zooKeeper = ZookeeperUtil.createZookeeper(connectSting,timeout);
    }


    @Override
    public void register(ServiceConfig<?> service) {
        //服务名称的节点（持久节点）
        String parentNode = Constant.BASE_PROVIDERS_PATH + "/" +service.getInterface().getName();
        if (!ZookeeperUtil.exists(zooKeeper,parentNode,null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(parentNode,null);
            ZookeeperUtil.createNode(zooKeeper,zookeeperNode,null, CreateMode.PERSISTENT);
        }
        //创建本机的临时节点,ip:port
        //服务提供方的端口一般自己设定，我们还需要一个获取ip的方法
        //ip我们通常是需要一个局域网ip，不是127.0.0.1，也不是ipv6
        //TODO 后续处理端口问题（全局保存端口的地方）
        String node = parentNode + "/" + NetUtils.getIp() + ":" + 8088;
        if (!ZookeeperUtil.exists(zooKeeper,node,null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(node,null);
            ZookeeperUtil.createNode(zooKeeper,zookeeperNode,null, CreateMode.EPHEMERAL);
        }

        if (log.isDebugEnabled()){
            log.debug("服务{}，已经被注册",service.getInterface().getName());
        }
    }

    @Override
    public InetSocketAddress lookUp(String serviceName) {
        //1.找到服务对应的节点
        String serviceNode = Constant.BASE_PROVIDERS_PATH + "/" + serviceName;

        //2.从zk中获取他的子节点,eg:192.168.12.122:1213
        List<String> childrens = ZookeeperUtil.getChildren(zooKeeper,serviceNode,null);
        // 获取了所有的可用的服务列表
        List<InetSocketAddress> inetSocketAddresses = childrens.stream().map(ipString -> {
            String[] ipAndPort = ipString.split(":");
            String ip = ipAndPort[0];
            int port = Integer.valueOf(ipAndPort[1]);
            return new InetSocketAddress(ip, port);
        }).collect(Collectors.toList());

        if (inetSocketAddresses.size() == 0){
            throw new DiscoveryException("未发现任何可用的服务主机");
        }
        //TODO 问题一：我们每次调用相关方法的时候都需要去注册中心拉取服务列表吗？ 本地缓存+watcher
        //     问题二：该如何合理的选择一个可用的服务，而不是只获取第一个。      负载均衡策略
        return inetSocketAddresses.get(0);
    }
}
