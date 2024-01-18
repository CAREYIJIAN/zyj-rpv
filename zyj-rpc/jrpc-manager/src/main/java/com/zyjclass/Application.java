package com.zyjclass;

import com.zyjclass.exceptions.ZookeeperException;
import com.zyjclass.utils.zookeeper.ZookeeperNode;
import com.zyjclass.utils.zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;

/**
 * 注册中心的管理页面
 * @author CAREYIJIAN$
 * @date 2024/1/17$
 */
@Slf4j
public class Application {
    public static void main(String[] args) {
        //创建zookeeper实例
        ZooKeeper zooKeeper = ZookeeperUtil.createZookeeper();
        //定义节点和数据
        ZookeeperNode basenode = new ZookeeperNode("/jrpc-metadata",null);
        ZookeeperNode providersnode = new ZookeeperNode("/jrpc-metadata/providers",null);
        ZookeeperNode consumersnode = new ZookeeperNode("/jrpc-metadata/consumers",null);
        //创建节点
        List.of(basenode,providersnode,consumersnode).forEach(zookeeperNode -> {
            ZookeeperUtil.createNode(zooKeeper,zookeeperNode,null,CreateMode.PERSISTENT);
        });
        //关闭连接
        ZookeeperUtil.close(zooKeeper);
    }

}
