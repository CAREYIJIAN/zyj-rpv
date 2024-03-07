package com.zyjclass.channelhandler.handler;

import com.zyjclass.JrpcBootstrap;
import com.zyjclass.ServiceConfig;
import com.zyjclass.core.ShutDownHolder;
import com.zyjclass.enumeration.RequestType;
import com.zyjclass.enumeration.RespCode;
import com.zyjclass.protection.RateLimiter;
import com.zyjclass.protection.TokenBuketRateLimiter;
import com.zyjclass.transport.message.JrpcRequest;
import com.zyjclass.transport.message.JrpcResponse;
import com.zyjclass.transport.message.RequestPayload;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Date;
import java.util.Map;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/20$
 */
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<JrpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, JrpcRequest jrpcRequest) throws Exception {
        //获得通道
        Channel channel = channelHandlerContext.channel();
        // 先封装统一封装的内容
        JrpcResponse jrpcResponse = new JrpcResponse();
        jrpcResponse.setRequestId(jrpcRequest.getRequestId());
        jrpcResponse.setCompressType(jrpcRequest.getCompressType());
        jrpcResponse.setSerializeType(jrpcRequest.getSerializeType());
        jrpcResponse.setTimeStamp(new Date().getTime());

        //查看关闭的挡板是否打开，如果已经打开，返回一个错误响应
        if (ShutDownHolder.BAFFLE.get()){
            jrpcResponse.setCode(RespCode.CLOSING.getCode());
            channel.writeAndFlush(jrpcResponse);
            return;
        }

        //计数器加一
        ShutDownHolder.REQUEST_COUNTER.increment();


        //限流策略：针对每个ip进行限流
        SocketAddress socketAddress = channel.remoteAddress();
        Map<SocketAddress, RateLimiter> everyIpRateLimiter = JrpcBootstrap.getInstance().getConfiguration().getEveryIpRateLimiter();
        RateLimiter rateLimiter = everyIpRateLimiter.get(socketAddress);
        if (rateLimiter == null){
            rateLimiter = new TokenBuketRateLimiter(100,100);
            everyIpRateLimiter.put(socketAddress,rateLimiter);
        }
        boolean allowRequest = rateLimiter.allowRequest();

        if (!allowRequest){  //限流
            //需要封装响应并且返回了
            jrpcResponse.setCode(RespCode.RATE_LIMIT.getCode());

        } else if ((jrpcRequest.getRequestType() == RequestType.HERT_BEAT.getId())){  //处理心跳
            jrpcResponse.setCode(RespCode.SUCCESS_HEART_BEAT.getCode());
        }else { //正常调用
            /**-----------------------------------------具体的调用过程------------------------------------------*/
            //1.获取负载内容
            RequestPayload requestPayload = jrpcRequest.getRequestPayload();

            //2.根据负载内容进行方法调用
            try {
                Object object = callTargetMethod(requestPayload);
                if (log.isDebugEnabled()) {
                    log.debug("请求【{}】已经在服务端完成方法调用。", jrpcRequest.getRequestId());
                }
                //3.封装响应
                jrpcResponse.setCode(RespCode.SUCCESS.getCode());
                jrpcResponse.setBody(object);
            }catch (Exception e){
                log.error("请求编号为【{}】在调用过程中发生异常。", jrpcRequest.getRequestId());
                jrpcResponse.setCode(RespCode.FAIL.getCode());
            }
        }
        //4.写出响应
        channel.writeAndFlush(jrpcResponse);

        //计数器减一
        ShutDownHolder.REQUEST_COUNTER.decrement();
    }

    private Object callTargetMethod(RequestPayload requestPayload) {
        String interfaceName = requestPayload.getInterfaceName();
        String methodName = requestPayload.getMethodName();
        Object[] parametersValue = requestPayload.getParametersValue();
        Class<?>[] parametsType = requestPayload.getParametsType();

        //寻找到匹配的暴露出去的具体实现
        ServiceConfig<?> serviceConfig = JrpcBootstrap.SERVICE_MAP.get(interfaceName);
        Object refImpl = serviceConfig.getRef();

        //通过反射调用（1.获取方法对象2.执行invoke方法）
        Object returnValue = null;
        try {
            Class<?> aClass = refImpl.getClass();
            Method method = aClass.getMethod(methodName, parametsType);
            returnValue = method.invoke(refImpl,parametersValue);
        }catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e){
            log.error("调用请求【{}】的方法【{}】时发生异常。",interfaceName,methodName,e);
            throw new RuntimeException(e);
        }
        return returnValue;

    }



}




















