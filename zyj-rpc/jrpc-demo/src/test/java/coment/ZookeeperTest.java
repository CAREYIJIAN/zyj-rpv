package coment;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;
import zyjrpcclass.zookeeper.MyWatcher;

import java.io.IOException;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/16$
 */
public class ZookeeperTest {

    ZooKeeper zooKeeper;

    @Before
    public void createZk(){
        //定义连接参数
        String connectString = "127.0.0.1:2181";
        //定义超时时间
        int timeout = 10000;
        try {
            //zooKeeper = new ZooKeeper(connectString,timeout,null);
            //new MyWatcher()设置默认的watcher
            zooKeeper = new ZooKeeper(connectString,timeout,new MyWatcher());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    //创建持久节点
    @Test
    public void testCreatePNode(){
        try {
            String result = zooKeeper.create("/jclass","PNode".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
            System.out.println("result = " + result);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            if (zooKeeper != null){
                try {
                    zooKeeper.close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    //删除节点
    @Test
    public void testDeleteNode(){
        try {
            //version: 类似与cas、mysql、乐观锁中的版本，选择-1可以无视版本号。
            zooKeeper.delete("/jclass",-1);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            if (zooKeeper != null){
                try {
                    zooKeeper.close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    //检查节点的版本号
    @Test
    public void testNodeStat(){
        try {
            Stat stat = zooKeeper.exists("/jclass", null);
            int version = stat.getVersion(); //当前节点的数据版本
            int aversion = stat.getAversion(); //当前节点的acl数据版本（每次修改acl权限都会加一）
            int cversion = stat.getCversion(); //当前子节点的数据版本
            System.out.println("version :"+version);
            System.out.println("aversion :"+aversion);
            System.out.println("cversion :"+cversion);

            //修改节点数据
            zooKeeper.setData("/jclass","hi".getBytes(),-1);
            System.out.println("version :"+version);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            if (zooKeeper != null){
                try {
                    zooKeeper.close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    //测试watcher
    @Test
    public void testWatcher(){
        try {
            //以下三个方法可以注册watcher，watch参数为true时使用默认的wather,也可以指定
            zooKeeper.exists("/jclass",true);
            //zooKeeper.getChildren();
            //zooKeeper.getData();

            while (true){
                Thread.sleep(10000);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            if (zooKeeper != null){
                try {
                    zooKeeper.close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }





}
