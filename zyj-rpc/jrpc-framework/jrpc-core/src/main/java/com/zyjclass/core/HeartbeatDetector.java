package com.zyjclass.core;

import com.zyjclass.JrpcBootstrap;
import com.zyjclass.NettyBootstrapInitializer;
import com.zyjclass.compress.CompressorFactory;
import com.zyjclass.discovery.Registry;
import com.zyjclass.enumeration.RequestType;
import com.zyjclass.serialize.SerializerFactory;
import com.zyjclass.transport.message.JrpcRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 心跳探测的核心目的是探活。那些是正常的，那些是不正常的
 * @author CAREYIJIAN$
 * @date 2024/1/25$
 */
@Slf4j
public class HeartbeatDetector {

    public static void detectHeartbeat(String serviceName){
        //从注册中心拉取服务列表并建立连接
        Registry registry = JrpcBootstrap.getInstance().getConfiguration().getRegistryConfig().getRegistry();
        List<InetSocketAddress> addresses = registry.lookUp(serviceName, JrpcBootstrap.getInstance().getConfiguration().getGroup());

        //将连接进行缓存
        for (InetSocketAddress address : addresses){
            try {
                if (!JrpcBootstrap.CHANNEL_MAP.containsKey(address)){
                    Channel channel = NettyBootstrapInitializer.getBootstrap().connect(address).sync().channel();
                    JrpcBootstrap.CHANNEL_MAP.put(address,channel);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        //定期发送心跳消息
        Thread thread = new Thread(() ->
                new Timer().scheduleAtFixedRate(new MyTimerTask(), 0, 2000),"jrpc-HeartbeatDetector-thread");
        thread.setDaemon(true);
        thread.start();
    }

    private static class MyTimerTask extends TimerTask{

        @Override
        public void run() {
            //将响应时长的map清空
            JrpcBootstrap.ANSWER_TIME_CHANNEL_MAP.clear();

            //遍历所有的channel
            Map<InetSocketAddress, Channel> channelMap = JrpcBootstrap.CHANNEL_MAP;
            for (Map.Entry<InetSocketAddress, Channel> entry: channelMap.entrySet()) {
                //定义重试的次数
                int tryTimes = 3;
                while (tryTimes > 0) {
                    Channel channel = entry.getValue();
                    long start = System.currentTimeMillis();
                    //构建一个心跳请求
                    JrpcRequest jrpcRequest = JrpcRequest.builder()
                            .requestId(JrpcBootstrap.getInstance().getConfiguration().getIdGenerator().getId())
                            .compressType(CompressorFactory.getCompressor(JrpcBootstrap.getInstance().getConfiguration().getCompressType()).getCode())
                            .requestType(RequestType.HERT_BEAT.getId())
                            .serializeType(SerializerFactory.getSerializer(JrpcBootstrap.getInstance().getConfiguration().getSerializeType()).getCode())
                            .timeStamp(start)
                            .build();

                    CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                    JrpcBootstrap.PENDING_MAP.put(jrpcRequest.getRequestId(), completableFuture);

                    channel.writeAndFlush(jrpcRequest).addListener((ChannelFutureListener) promise -> {
                        if (!promise.isSuccess()) {
                            completableFuture.completeExceptionally(promise.cause());
                        }
                    });

                    //
                    Long endTime = 0L;
                    try {
                        //阻塞方法，get（）方法如果得不到结果，就会一直阻塞
                        //若想不一直阻塞可添加参数
                        completableFuture.get(1, TimeUnit.SECONDS);
                        endTime = System.currentTimeMillis();
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        //发生问题优先重试
                        tryTimes --;
                        log.error("和地址为【{}】的主机连接发生异常,正在进行第【{}】次重试...", channel.remoteAddress(),3 - tryTimes);
                        //重试机会用尽，将失效的地址移除服务列表
                        if (tryTimes == 0){
                            JrpcBootstrap.CHANNEL_MAP.remove(entry.getKey());
                        }

                        //等一段时间后重试,防止重试风暴
                        try {
                            Thread.sleep(10 * (new Random().nextInt(5)));
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                        continue;
                    }

                    Long time = endTime - start;

                    //使用treemap进行缓存
                    JrpcBootstrap.ANSWER_TIME_CHANNEL_MAP.put(time, channel);
                    log.debug("和【{}】服务器的响应时间是【{}】", entry.getKey(), time);
                    break;
                }
            }
            log.info("---------------------------------------响应时间的treemap-----------------------------------");
            for (Map.Entry<Long, Channel> entry : JrpcBootstrap.ANSWER_TIME_CHANNEL_MAP.entrySet()){
                if (log.isDebugEnabled()){
                    log.debug("【{}】--->channelId:【{}】",entry.getKey(),entry.getValue());
                }
            }

        }
    }



}
