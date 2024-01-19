package com.zyjclass.discovery.impl;

import com.zyjclass.Constant;
import com.zyjclass.ServiceConfig;
import com.zyjclass.discovery.AbstractRegistry;
import com.zyjclass.utils.NetUtils;
import com.zyjclass.utils.zookeeper.ZookeeperNode;
import com.zyjclass.utils.zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/18$
 */
@Slf4j
public class NacosRegistry extends AbstractRegistry {
    //TODO 待修改
    //维护一个zookeeper实例
    private ZooKeeper zooKeeper;
    public NacosRegistry(){
        this.zooKeeper = ZookeeperUtil.createZookeeper();
    }
    public NacosRegistry(String connectSting, int timeout){
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
        return null;
    }
}
