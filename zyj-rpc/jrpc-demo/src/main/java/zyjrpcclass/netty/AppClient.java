package zyjrpcclass.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/15$
 */
//客户端示例
public class AppClient {

    public void run(){
        //定义干活的线程池，I/O线程池
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bs = new Bootstrap();//客户端辅助启动类
            bs.group(group)
                    .channel(NioSocketChannel.class)//通过工厂方法设计模式实例化一个Channel
                    .remoteAddress(new InetSocketAddress(8080))//绑定远端的ip和地址
                    .handler(new ChannelInitializer<SocketChannel>(){//进行通道的初始化配置
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception{
                            //添加我们自定义的Handler
                            socketChannel.pipeline().addLast(new MyChannelHandLer2());
                        }
                    });
            //连接到远程节点，等待连接完成
            ChannelFuture future = bs.connect().sync();
            //发送消息到服务端，编码格式是utf-8
            future.channel().writeAndFlush(Unpooled.copiedBuffer("Hello world", CharsetUtil.UTF_8));
            //阻塞操作，closeFuture()开启了一个channel的监听器（这期间channle在进行各项工作(接收消息)直到链路断开）
            future.channel().closeFuture().sync();
        }catch (InterruptedException e){
            e.printStackTrace();
        }finally {
            try {
                group.shutdownGracefully().sync();//优雅的关闭
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String [] args) throws Exception
    {
        new AppClient().run();
    }


}
