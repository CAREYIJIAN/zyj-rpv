package com.zyjclass.channelhandler.handler;

import com.zyjclass.compress.Compressor;
import com.zyjclass.compress.CompressorFactory;
import com.zyjclass.enumeration.RequestType;
import com.zyjclass.serialize.Serializer;
import com.zyjclass.serialize.SerializerFactory;
import com.zyjclass.transport.message.JrpcRequest;
import com.zyjclass.transport.message.MessageFormatConstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

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
public class JrpcRequestEncoder extends MessageToByteEncoder<JrpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, JrpcRequest jrpcRequest, ByteBuf byteBuf) throws Exception {
        //魔数值
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        //版本号
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        //头部的长度
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
        //总长度（不能确定，不知道body）writerIndex(将写指针移到哪个位置上) writerIndex获取当前写指针位置
        byteBuf.writerIndex(byteBuf.writerIndex() + MessageFormatConstant.FULL_FIELD_LENGTH);
        //三个类型
        byteBuf.writeByte(jrpcRequest.getRequestType());
        byteBuf.writeByte(jrpcRequest.getSerializeType());
        byteBuf.writeByte(jrpcRequest.getCompressType());
        //请求id
        byteBuf.writeLong(jrpcRequest.getRequestId());
        //时间戳
        byteBuf.writeLong(jrpcRequest.getTimeStamp());
        //注意：心跳请求不处理请求体
        /*if (jrpcRequest.getRequestType() == RequestType.HERT_BEAT.getId()){
            return;
        }*/
        //body (requestPayload)
        //1.根据配置的序列化方式进行序列化
        //  方案一： 直接使用工具类，耦合性很高 如果之后想要换序列化的方式，很不方便。
        //  byte[] serialize = SerializeUtil.serialize(jrpcRequest.getRequestPayload());
        //  方案二：面向抽象编程（推荐）
        byte[] body = null;
        if (jrpcRequest.getRequestPayload() != null){
            Serializer serializer = SerializerFactory.getSerializer(jrpcRequest.getSerializeType()).getImpl();
            body = serializer.serialize(jrpcRequest.getRequestPayload());

            //2.根据配置的压缩方式进行压缩
            Compressor compressor = CompressorFactory.getCompressor(jrpcRequest.getCompressType()).getImpl();
            body = compressor.compress(body);
        }

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
            log.debug("请求【{}】已经完成报文的编码。",jrpcRequest.getRequestId());
        }
    }

}




















