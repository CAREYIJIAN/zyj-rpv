package zyjrpcclass.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/15$
 */
//服务端示例
public class AppServer {
    private int port;

    public AppServer(int port){
        this.port = port;
    }

    public void start(){
        //创建eventloop，老板只负责处理请求，之后会将请求分发至worker
        EventLoopGroup boss = new NioEventLoopGroup(2);
        EventLoopGroup worker = new NioEventLoopGroup(10);
        try {
            //服务器引导程序
            ServerBootstrap b = new ServerBootstrap();//用于启动NIO服务
            //配置服务器
            b.group(boss,worker)
                    .channel(NioServerSocketChannel.class)//通过工厂方法设计模式实例化一个channel
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new MyChannelHandLer());
                        }
                    });
            //绑定服务器，该实例将提供有关IO操作的结果或状态信息
            ChannelFuture channelFuture = b.bind().sync();
            System.out.println();
            channelFuture.channel().closeFuture().sync();
        }catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            try {
                boss.shutdownGracefully().sync();
                worker.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String [] args) throws Exception{
        new AppServer(8080).start();
    }
}




















