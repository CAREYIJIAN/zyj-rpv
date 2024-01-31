package com.zyjclass.channelhandler.handler;

import com.zyjclass.JrpcBootstrap;
import com.zyjclass.enumeration.RespCode;
import com.zyjclass.exceptions.ResponseException;
import com.zyjclass.loadbalancer.LoadBalancer;
import com.zyjclass.protection.CircuitBreaker;
import com.zyjclass.transport.message.JrpcRequest;
import com.zyjclass.transport.message.JrpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Map;
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

        //从全局的挂起的请求中寻找与之匹配的待处理的completableFuture
        CompletableFuture<Object> completableFuture = JrpcBootstrap.PENDING_MAP.get(jrpcResponse.getRequestId());

        SocketAddress socketAddress = channelHandlerContext.channel().remoteAddress();
        Map<SocketAddress, CircuitBreaker> everyIpCircuitBreaker = JrpcBootstrap.getInstance().getConfiguration().getEveryIpCircuitBreaker();
        CircuitBreaker circuitBreaker = everyIpCircuitBreaker.get(socketAddress);

        byte code = jrpcResponse.getCode();
        if (code == RespCode.FAIL.getCode()){
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("当前id为【{}】的请求，返回错误，响应码：【{}】",jrpcResponse.getRequestId(),code);
            throw new ResponseException(code, RespCode.FAIL.getDesc());

        }else if (code == RespCode.RATE_LIMIT.getCode()){
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("当前id为【{}】的请求，被限流，响应码：【{}】",jrpcResponse.getRequestId(),code);
            throw new ResponseException(code, RespCode.RATE_LIMIT.getDesc());

        }else if (code == RespCode.RESOURCE_NOT_FOUND.getCode()){
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("当前id为【{}】的请求，未找到资源，响应码：【{}】",jrpcResponse.getRequestId(),code);
            throw new ResponseException(code, RespCode.RESOURCE_NOT_FOUND.getDesc());

        }else if (code == RespCode.SUCCESS.getCode()){
            //服务提供方给定结果
            Object returnValue = jrpcResponse.getBody();
            completableFuture.complete(returnValue);
            if (log.isDebugEnabled()){
                log.debug("已寻找到编号为【{}】的completableFuture，处理响应结果",jrpcResponse.getRequestId());
            }
        }else if (code == RespCode.SUCCESS_HEART_BEAT.getCode()){
            completableFuture.complete(null);
            if (log.isDebugEnabled()){
                log.debug("已寻找到编号为【{}】的completableFuture，处理心跳检测",jrpcResponse.getRequestId());
            }
        }else if (code == RespCode.CLOSING.getCode()){
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            if (log.isDebugEnabled()){
                log.debug("当前id为【{}】的请求，访问被拒绝，目标服务器正处于关闭中，响应码：【{}】",jrpcResponse.getRequestId(),code);
            }

            //修正负载均衡器
            //从健康列表中移除
            JrpcBootstrap.CHANNEL_MAP.remove(socketAddress);
            //找到负载均衡器进行reLoadBalance
            LoadBalancer loadBalancer = JrpcBootstrap.getInstance().getConfiguration().getLoadBalancer();
            JrpcRequest jrpcRequest = JrpcBootstrap.REQUEST_THREAD_LOCAL.get();
            loadBalancer.reLoadBalance(jrpcRequest.getRequestPayload().getInterfaceName(),JrpcBootstrap.CHANNEL_MAP.keySet().stream().toList());

            throw new ResponseException(code, RespCode.CLOSING.getDesc());
        }
    }

}
