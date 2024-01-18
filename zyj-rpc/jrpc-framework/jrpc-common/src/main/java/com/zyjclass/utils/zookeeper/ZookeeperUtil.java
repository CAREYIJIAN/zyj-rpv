package com.zyjclass.utils.zookeeper;

import com.zyjclass.Constant;
import com.zyjclass.exceptions.ZookeeperException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/17$
 */
@Slf4j
public class ZookeeperUtil {
    /**
     * 使用默认配置创建zookeeper实例
     * @return ZooKeeper
     */
    public static ZooKeeper createZookeeper(){
        //定义连接参数
        String connectString = Constant.Default_ZK_CONNECT;
        //定义超时时间
        int timeout = Constant.TIME_OUT;
        return createZookeeper(connectString,timeout);
    }

    public static ZooKeeper createZookeeper(String connectString, int timeout){
        CountDownLatch countDownLatch = new CountDownLatch(1);

        try {
            //创建zookeeper实例
            final ZooKeeper zooKeeper = new ZooKeeper(connectString,timeout, event -> {
                //只有连接成功才放行
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected){
                    System.out.println("客户端已经连接成功");
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
            return zooKeeper;
        } catch (IOException | InterruptedException e) {
            log.error("创建zookeeper时，产生异常：",e);
            throw new ZookeeperException();
        }
    }

    /**
     * 创建一个节点的工具方法
     * @param zooKeeper 实例
     * @param node 节点
     * @param watcher watcher实例
     * @param createMode 节点的类型
     * @return Boolean
     */
    public static Boolean createNode(ZooKeeper zooKeeper, ZookeeperNode node, Watcher watcher, CreateMode createMode){
        try {
            if (zooKeeper.exists(node.getNodePath(),watcher) == null){
                String result = zooKeeper.create(node.getNodePath(),node.getData(),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
                log.info("根节点【{}】，成功创建",result);
                return true;
            } else {
                if (log.isDebugEnabled()){
                    log.debug("节点【{}】已经存在，无需创建。",node.getNodePath());
                }
                return false;
            }
        } catch (KeeperException | InterruptedException e) {
            log.error("创建基础目录时产生异常：",e);
            throw new ZookeeperException();
        }
    }

    /**
     * 关闭zookeeper工具方法
     * @param zooKeeper
     */
    public static void close (ZooKeeper zooKeeper){
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            log.error("关闭zookeeper出现异常：",e);
            throw new ZookeeperException();
        }
    }
}
