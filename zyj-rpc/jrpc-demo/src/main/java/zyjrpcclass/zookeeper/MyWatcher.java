package zyjrpcclass.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/16$
 */
//在正式使用的时候，自己的watcher只用关心自己需要的时间就可以，这里只是一些练习
public class MyWatcher implements Watcher {

    @Override
    public void process(WatchedEvent event) {
        //判断时间类型(连接类型的事件)
        if (event.getType() == Event.EventType.None){
            if (event.getState() == Event.KeeperState.SyncConnected){
                System.out.println("zookeeper连接成功");
            } else if (event.getState() == Event.KeeperState.AuthFailed) {
                System.out.println("zookeeper认证失败");
            } else if (event.getState() == Event.KeeperState.Disconnected) {
                System.out.println("zookeeper断开连接");
            }
        } else if (event.getType() == Event.EventType.NodeCreated) {
            System.out.println(event.getPath() + "被创建了");
        } else if (event.getType() == Event.EventType.NodeDeleted) {
            System.out.println(event.getPath() + "被删除了");
        } else if (event.getType() == Event.EventType.NodeDataChanged) {
            System.out.println(event.getPath() + "节点的数据被修改了");
        } else if (event.getType() == Event.EventType.NodeChildrenChanged) {
            System.out.println(event.getPath() + "子节点发生了变化");
        }
    }
}
