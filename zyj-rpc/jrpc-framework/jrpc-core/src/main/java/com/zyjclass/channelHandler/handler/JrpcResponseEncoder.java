package com.zyjclass.channelHandler.handler;

import com.zyjclass.enumeration.RequestType;
import com.zyjclass.transport.message.JrpcRequest;
import com.zyjclass.transport.message.JrpcResponse;
import com.zyjclass.transport.message.MessageFormatConstant;
import com.zyjclass.transport.message.RequestPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * 出站时，第一个经过的处理器
 * 4byte  magic(魔数)  --->"jrpc".getBytes()
 * 1byte  version(版本)  --->1
 * 2byte  header length(首部的长度)
 * 4byte  full length(报文总长度)
 * 1byte  serialize
 * 1byte  compress
 * 1byte  requestType
 * 8byte  requestId
 * body
 *
 * @author CAREYIJIAN$
 * @date 2024/1/20$
 */
@Slf4j
public class JrpcResponseEncoder extends MessageToByteEncoder<JrpcResponse> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, JrpcResponse jrpcResponse, ByteBuf byteBuf) throws Exception {
        //魔数值
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        //版本号
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        //头部的长度
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
        //总长度（不能确定，不知道body）writerIndex(将写指针移到哪个位置上) writerIndex获取当前写指针位置
        byteBuf.writerIndex(byteBuf.writerIndex() + MessageFormatConstant.FULL_FIELD_LENGTH);
        //三个类型
        byteBuf.writeByte(jrpcResponse.getCode());
        byteBuf.writeByte(jrpcResponse.getSerializeType());
        byteBuf.writeByte(jrpcResponse.getCompressType());
        //请求id
        byteBuf.writeLong(jrpcResponse.getRequestId());
        /*//注意：心跳请求不处理请求体
        if (jrpcResponse.getRequestType() == RequestType.HERT_BEAT.getId()){
            return;
        }*/
        //body
        byte[] body = getBodyBytes(jrpcResponse.getBody());
        if (body != null){
            byteBuf.writeBytes(body);
        }
        int bodyLength = body == null ? 0 : body.length;
        //重新处理报文的总长度，先保存当前写指针的位置，再将写指针的位置移动到总长度(full length)的位置上。
        int writerIndex = byteBuf.writerIndex();
        byteBuf.writerIndex(MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH);
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyLength);
        //将写指针归位
        byteBuf.writerIndex(writerIndex);

        if (log.isDebugEnabled()){
            log.debug("响应【{}】已经在服务端完成编码工作。",jrpcResponse.getRequestId());
        }
    }

    private byte[] getBodyBytes(Object body) {
        //心跳的请求没有payload
        if (body == null){
            return null;
        }
        //对象怎么变成一个字节数据 序列化 压缩
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(baos);) {
            outputStream.writeObject(body);
            //压缩

            return baos.toByteArray();
        } catch (IOException e) {
            log.error("序列化时出现异常");
            throw new RuntimeException(e);
        }
    }
}




















