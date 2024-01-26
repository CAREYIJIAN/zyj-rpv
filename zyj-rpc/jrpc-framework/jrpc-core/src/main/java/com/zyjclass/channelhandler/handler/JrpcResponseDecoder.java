package com.zyjclass.channelhandler.handler;

import com.zyjclass.compress.Compressor;
import com.zyjclass.compress.CompressorFactory;
import com.zyjclass.serialize.Serializer;
import com.zyjclass.serialize.SerializerFactory;
import com.zyjclass.transport.message.JrpcResponse;
import com.zyjclass.transport.message.MessageFormatConstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * 基于长度字段的帧解码器
 * @author CAREYIJIAN$
 * @date 2024/1/20$
 */
@Slf4j
public class JrpcResponseDecoder extends LengthFieldBasedFrameDecoder {
    public JrpcResponseDecoder() {
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
        byte responseCode = byteBuf.readByte();

        //6.解析序列化类型
        byte serializeType = byteBuf.readByte();

        //7.解析压缩类型
        byte compressType = byteBuf.readByte();

        //8.解析请求id
        long requestId = byteBuf.readLong();

        //9.时间戳
        long timeStamp = byteBuf.readLong();

        //封装为jrpcRequest
        JrpcResponse jrpcResponse = new JrpcResponse();
        jrpcResponse.setCode(responseCode);
        jrpcResponse.setCompressType(compressType);
        jrpcResponse.setSerializeType(serializeType);
        jrpcResponse.setRequestId(requestId);
        jrpcResponse.setTimeStamp(timeStamp);

        //注意：心跳没有负载，判断后直接返回
        /*if (responseCode == RequestType.HERT_BEAT.getId()){
            return jrpcResponse;
        }*/

        int bodyLength = fullLength - headLength;
        byte[] body = new byte[bodyLength];
        byteBuf.readBytes(body);
        if (body.length > 0){
            //有了字节数组之后就可以解压缩、反序列化
            Compressor compressor = CompressorFactory.getCompressor(compressType).getCompressor();
            body = compressor.decompress(body);
            //反序列化
            Serializer serializer = SerializerFactory.getSerializer(serializeType).getSerializer();
            Object deserialize = serializer.deserialize(body, Object.class);
            jrpcResponse.setBody(deserialize);
        }

        if (log.isDebugEnabled()){
            log.debug("响应【{}】已经在调用端完成解码工作。",jrpcResponse.getRequestId());
        }

        return jrpcResponse;

    }


}
