package com.zyjclass.channelHandler.handler;

import com.zyjclass.JrpcBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

/**
 * 用来测试的类
 * @author CAREYIJIAN$
 * @date 2024/1/19$
 */
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        //服务提供方给定结果
        ByteBuf byteBuf = (ByteBuf) msg;
        String result = byteBuf.toString(Charset.defaultCharset());
        //从全局的挂起的请求中寻找与之匹配的待处理的completableFuture
        CompletableFuture<Object> completableFuture = JrpcBootstrap.PENDING_MAP.get(1L);
        completableFuture.complete(result);

    }

}
