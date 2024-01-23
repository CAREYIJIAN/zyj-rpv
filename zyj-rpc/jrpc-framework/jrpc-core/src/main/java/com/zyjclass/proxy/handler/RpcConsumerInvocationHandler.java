package com.zyjclass.proxy.handler;

import com.zyjclass.JrpcBootstrap;
import com.zyjclass.NettyBootstrapInitializer;
import com.zyjclass.discovery.Registry;
import com.zyjclass.enumeration.RequestType;
import com.zyjclass.exceptions.DiscoveryException;
import com.zyjclass.exceptions.NetworkException;
import com.zyjclass.transport.message.JrpcRequest;
import com.zyjclass.transport.message.RequestPayload;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 该类封装了客户端通信的基础逻辑，每一个代理对象的远程调用过程都封装在了invoke方法中
 * 1. 发现可用服务  2.建立连接  3.封装报文  4.发送请求   5.得到结果
 * @author CAREYIJIAN$
 * @date 2024/1/19$
 */
@Slf4j
public class RpcConsumerInvocationHandler implements InvocationHandler {

    private final Registry registry;//注册中心

    private final Class<?> interfaceRef;//接口

    public RpcConsumerInvocationHandler(Registry registry, Class<?> interfaceRef) {
        this.registry = registry;
        this.interfaceRef = interfaceRef;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //我们调用syHi事实上会走进这个代码段中，我们已经知道method，args
        log.info("method-->{}",method.getName());
        log.info("args-->{}",args);

        //1.发现服务，从注册中心寻找一个可用发服务
        //传入服务的名字,返回一个ip+端口
        InetSocketAddress address = registry.lookUp(interfaceRef.getName());
        if (log.isDebugEnabled()){
            log.debug("服务调用方发现了服务【{}】的可用主机【{}】", interfaceRef, address);
        }
        //使用netty连接服务器，发送调用的服务的名字+方法名+参数列表，返回结果
        /**
         * 思考：如果连接过程放在这里，那就意味着每一次调用都会产生一个netty连接，对于长连接来讲显然是不合适的
         * 那么如何缓存我们的连接？
         * 解决方案：缓存channel，尝试从缓存中获取channel，如果获取不到则创建并缓存。
         */
        //2.从缓存中获取一个可用通道(建立连接)
        Channel channel = getAvailableChannel(address);
        if (log.isDebugEnabled()){
            log.debug("获取了和【{}】建立的连接通道，准备发送数据", address);
        }
        /*-------------------------------------------封装报文---------------------------------------------*/

        RequestPayload requestPayload = RequestPayload.builder()
                .interfaceName(interfaceRef.getName())
                .methodName(method.getName())
                .parametsType(method.getParameterTypes())
                .parametersValue(args).returnType(method.getReturnType()).build();

        JrpcRequest jrpcRequest = JrpcRequest.builder()
                .requestId(JrpcBootstrap.ID_GENERATOR.getId())
                .compressType((byte) 1)
                .requestType(RequestType.REQUEST.getId())
                .serializeType((byte) 1)
                .requestPayload(requestPayload).build();
        /*-------------------------------------------发送(同步策略)---------------------------------------------*/
        //ChannelFuture channelFuture = channel.writeAndFlush(null);
        //channelFuture的简单api: get（阻塞获取结果），getNow（获取当前结果，如果未处理完成返回null）
                /*if (channelFuture.isDone()){
                    Object object = channelFuture.getNow();
                }else if (!channelFuture.isSuccess()){
                    //需要捕获异常，可以捕获异步任务中的异常
                    Throwable cause = channelFuture.cause();
                    throw new RuntimeException(cause);
                }*/
        /*-------------------------------------------发送(异步策略)---------------------------------------------*/
        //4.发送报文
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        JrpcBootstrap.PENDING_MAP.put(1L,completableFuture);

        //这里writeAndFlush写出一个请求，这个请求实例就会进入pipeline执行出站的一系列操作
        //第一个出站程序一定是将 jrpcRequest转换为二进制的报文
        channel.writeAndFlush(jrpcRequest).addListener((ChannelFutureListener) promise -> {
            //将completableFuture挂起并暴露，并且在得到服务提供方的响应的时候调用complete方法

            if (!promise.isSuccess()) {
                completableFuture.completeExceptionally(promise.cause());
            }
        });

        //如果没有地方处理这个completableFuture，这里会阻塞等待complete方法执行（pipeline中最终的handler处理中）。
        //5.获取结果
        return completableFuture.get(10,TimeUnit.SECONDS);
    }

    /**
     * 根据地址获取一个可用的通道
     * @param address
     * @return
     */
    private Channel getAvailableChannel(InetSocketAddress address) {
        //尝试从缓存中获取
        Channel channel = JrpcBootstrap.CHANNEL_MAP.get(address);
        if (channel == null){
            //创建channel，连接到远程节点，等待连接完成
            // await 方法会阻塞，等待连接成功再返回。ps：netty还提供了异步的处理方法。
            /**
             * sync和await都是阻塞当前线程，获取返回值。（因为在连接和发送数据的过程是异步的，所以要用sync或await阻塞一下）
             * sync：如果发送异常，它会主动在主线程抛出异常。
             * await：如果发送异常，异常在子线程中处理需要使用future中处理。
             */
            //同步策略
            //channel = NettyBootstrapInitializer.getBootstrap().connect(address).await().channel();
            //异步策略
            CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
            NettyBootstrapInitializer.getBootstrap().connect(address).addListener((ChannelFutureListener) promise -> {
                if (promise.isDone()){
                    //异步
                    if (log.isDebugEnabled()){
                        log.debug("已经和【{}】成功建立了连接。",address);
                    }
                    channelFuture.complete(promise.channel());
                } else if (!promise.isSuccess()) {
                    channelFuture.completeExceptionally(promise.cause());
                }
            });
            //阻塞获取channel
            try {
                channel = channelFuture.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("获取通道时发生异常。",e);
                throw new DiscoveryException(e);
            }
            //缓存channel
            JrpcBootstrap.CHANNEL_MAP.put(address,channel);
        }
        if (channel == null){
            log.error("获取或建立与【{}】通道时发生异常",address);
            throw new NetworkException("获取通道时发生异常");
        }
        return channel;
    }
}
