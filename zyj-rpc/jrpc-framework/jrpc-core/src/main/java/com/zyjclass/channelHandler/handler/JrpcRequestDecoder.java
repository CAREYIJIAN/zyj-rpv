package com.zyjclass.channelHandler.handler;

import com.zyjclass.enumeration.RequestType;
import com.zyjclass.serialize.Serializer;
import com.zyjclass.serialize.SerializerFactory;
import com.zyjclass.serialize.SerializerWrapper;
import com.zyjclass.transport.message.JrpcRequest;
import com.zyjclass.transport.message.MessageFormatConstant;
import com.zyjclass.transport.message.RequestPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * 基于长度字段的帧解码器
 * @author CAREYIJIAN$
 * @date 2024/1/20$
 */
@Slf4j
public class JrpcRequestDecoder extends LengthFieldBasedFrameDecoder {
    public JrpcRequestDecoder() {
        //找到当前报文的总长度，截取报文，截取出来的报文再进行解析
        super(
                MessageFormatConstant.MAX_FRAME_LENGTH,//最大帧的长度，超过这个值会直接丢弃
                MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH,//长度的字段的偏移量
                MessageFormatConstant.FULL_FIELD_LENGTH,//总长度的字段的长度
                -(MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH + MessageFormatConstant.FULL_FIELD_LENGTH),//负载的适配长度
                0//跳过的一些字段
        );
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception{
        Object decode = super.decode(ctx, in);
        if (decode instanceof ByteBuf byteBuf){
            return decodeFrame(byteBuf);
        }

        return null;
    }

    private Object decodeFrame(ByteBuf byteBuf) {
        //注：byteBuf读取按顺序来读，因为byteBuf是根据指针往下走的。

        //1.解析魔数值
        byte[] magic = new byte[MessageFormatConstant.MAGIC.length];
        byteBuf.readBytes(magic);
        //检测魔数是否匹配
        for (int i = 0; i < magic.length; i++){
            if (magic[i] != MessageFormatConstant.MAGIC[i]){
                throw new RuntimeException("获得的请求不合法。");
            }

        }

        //2.解析版本号
        byte version = byteBuf.readByte();
        if (version > MessageFormatConstant.VERSION){
            throw new RuntimeException("获得的版本不支持。");
        }

        //3.解析头部的长度
        short headLength = byteBuf.readShort();

        //4.解析总长度
        int fullLength = byteBuf.readInt();

        //5.解析请求的类型 （判断是不是心跳检测）
        byte requestType = byteBuf.readByte();

        //6.解析序列化类型
        byte serializeType = byteBuf.readByte();

        //7.解析压缩类型
        byte compressType = byteBuf.readByte();

        //8.解析请求id
        long requestId = byteBuf.readLong();

        //封装为jrpcRequest
        JrpcRequest jrpcRequest = new JrpcRequest();
        jrpcRequest.setRequestType(requestType);
        jrpcRequest.setCompressType(compressType);
        jrpcRequest.setSerializeType(serializeType);
        jrpcRequest.setRequestId(requestId);

        //注意：心跳请求没有负载，判断后直接返回
        if (requestType == RequestType.HERT_BEAT.getId()){
            return jrpcRequest;
        }

        int payLoadLength = fullLength - headLength;
        byte[] payload = new byte[payLoadLength];
        byteBuf.readBytes(payload);
        //有了字节数组之后就可以解压缩、反序列化

        //反序列化
        Serializer serializer = SerializerFactory.getSerializer(serializeType).getSerializer();
        RequestPayload requestPayload = serializer.deserialize(payload, RequestPayload.class);
        jrpcRequest.setRequestPayload(requestPayload);

        if (log.isDebugEnabled()){
            log.debug("请求【{}】已经在服务端完成解码操作。",jrpcRequest.getRequestId());
        }

        return jrpcRequest;

    }


}
