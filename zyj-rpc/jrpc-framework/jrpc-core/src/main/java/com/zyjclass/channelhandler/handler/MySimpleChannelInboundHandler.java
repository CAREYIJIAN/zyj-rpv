package com.zyjclass.channelhandler.handler;

import com.zyjclass.JrpcBootstrap;
import com.zyjclass.transport.message.JrpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * 用来测试的类
 * @author CAREYIJIAN$
 * @date 2024/1/19$
 */
@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<JrpcResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, JrpcResponse jrpcResponse) throws Exception {
        //服务提供方给定结果
        Object returnValue = jrpcResponse.getBody();
        //从全局的挂起的请求中寻找与之匹配的待处理的completableFuture
        CompletableFuture<Object> completableFuture = JrpcBootstrap.PENDING_MAP.get(1L);
        completableFuture.complete(returnValue);
        if (log.isDebugEnabled()){
            log.debug("已寻找到编号为【{}】的completableFuture。",jrpcResponse.getRequestId());
        }

    }

}
