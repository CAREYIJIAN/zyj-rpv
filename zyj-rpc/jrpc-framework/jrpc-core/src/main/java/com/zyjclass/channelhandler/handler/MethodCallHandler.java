package com.zyjclass.channelhandler.handler;

import com.zyjclass.JrpcBootstrap;
import com.zyjclass.ServiceConfig;
import com.zyjclass.enumeration.RespCode;
import com.zyjclass.transport.message.JrpcRequest;
import com.zyjclass.transport.message.JrpcResponse;
import com.zyjclass.transport.message.RequestPayload;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/20$
 */
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<JrpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, JrpcRequest jrpcRequest) throws Exception {
        //1.获取负载内容
        RequestPayload requestPayload = jrpcRequest.getRequestPayload();

        //2.根据负载内容进行方法调用
        Object object = callTargetMethod(requestPayload);
        if (log.isDebugEnabled()){
            log.debug("请求【{}】已经在服务端完成方法调用。",jrpcRequest.getRequestId());
        }

        //3.封装响应
        JrpcResponse jrpcResponse = new JrpcResponse();
        jrpcResponse.setCode(RespCode.SUCCESS.getCode());
        jrpcResponse.setRequestId(jrpcRequest.getRequestId());
        jrpcResponse.setCompressType(jrpcRequest.getCompressType());
        jrpcResponse.setSerializeType(jrpcRequest.getSerializeType());
        jrpcResponse.setBody(object);


        //4.写出响应
        channelHandlerContext.channel().writeAndFlush(jrpcResponse);



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




















