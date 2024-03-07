package com.zyjclass;

import com.zyjclass.channelhandler.ConsumerChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 提供bootstrap单例
 * @author CAREYIJIAN$
 * @date 2024/1/19$
 */
@Slf4j
public class NettyBootstrapInitializer {

    private static final Bootstrap bootstrap = new Bootstrap();
    static{
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)//通过工厂方法设计模式实例化一个Channel
                .handler(new ConsumerChannelInitializer());
    }
    private NettyBootstrapInitializer(){}

    public static Bootstrap getBootstrap(){
        return bootstrap;
    }

}
