package com.zyjclass.channelHandler;

import com.zyjclass.channelHandler.handler.MySimpleChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/19$
 */
public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        //添加我们自定义的Handler
        socketChannel.pipeline().addLast(new MySimpleChannelInboundHandler());
    }
}
