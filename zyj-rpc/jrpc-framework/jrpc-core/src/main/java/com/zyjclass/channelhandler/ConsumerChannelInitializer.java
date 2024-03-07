package com.zyjclass.channelhandler;

import com.zyjclass.channelhandler.handler.JrpcRequestEncoder;
import com.zyjclass.channelhandler.handler.JrpcResponseDecoder;
import com.zyjclass.channelhandler.handler.MySimpleChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/19$
 */
public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        //添加我们自定义的Handler
        socketChannel.pipeline()
                //加一个netty自带的日志处理器
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                //消息编码器（出站）
                .addLast(new JrpcRequestEncoder())
                //入栈的解码器
                .addLast(new JrpcResponseDecoder())
                //处理结果
                .addLast(new MySimpleChannelInboundHandler());
    }
}
