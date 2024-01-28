package com.zyjclass.watch;

import com.zyjclass.JrpcBootstrap;
import com.zyjclass.NettyBootstrapInitializer;
import com.zyjclass.discovery.Registry;
import com.zyjclass.loadbalancer.LoadBalancer;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/26$
 */
@Slf4j
public class UpAndDownWatcher implements Watcher {
    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.NodeChildrenChanged){
            if (log.isDebugEnabled()){
                log.debug("检测到服务【{}】下有节点上/下线，将重新拉取服务列表..",event.getPath());
            }
            String serviceName = getServiceName(event.getPath());
            Registry registry = JrpcBootstrap.getInstance().getConfiguration().getRegistryConfig().getRegistry();
            List<InetSocketAddress> addresses = registry.lookUp(serviceName);
            //处理上线的节点
            for (InetSocketAddress address : addresses) {
                //上线的节点会  在address  不在CHANNEL_MAP
                if (!JrpcBootstrap.CHANNEL_MAP.containsKey(address)){
                    //根据地址建立连接，并且缓存
                    Channel channel = null;
                    try {
                        channel = NettyBootstrapInitializer.getBootstrap().connect(address).sync().channel();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    JrpcBootstrap.CHANNEL_MAP.put(address,channel);
                }
            }

            //处理下线的节点
            //下线的节点会  不在address  可能在CHANNEL_MAP
            for (Map.Entry<InetSocketAddress,Channel> entry : JrpcBootstrap.CHANNEL_MAP.entrySet()){
                if (!addresses.contains(entry.getKey())){
                    JrpcBootstrap.CHANNEL_MAP.remove(entry.getKey());
                }
            }

            //获得负载均衡器，进行重新loadBalance
            LoadBalancer lodaBalancer = JrpcBootstrap.getInstance().getConfiguration().getLoadBalancer();
            lodaBalancer.reLoadBalance(serviceName,addresses);

        }

    }

    private String getServiceName(String path) {
        String[] split = path.split("/");
        return split[split.length - 1];
    }
}
