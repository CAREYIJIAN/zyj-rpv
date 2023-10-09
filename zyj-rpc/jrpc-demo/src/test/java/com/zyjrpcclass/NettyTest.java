package com.zyjrpcclass;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

public class NettyTest {
    @Test
    public void testByteBuf(){
        ByteBuf byteBuf = Unpooled.buffer();
    }

    @Test
    public void testCompositeByteBuf(){
        ByteBuf header = Unpooled.buffer();
        ByteBuf body = Unpooled.buffer();

        //通过逻辑组装而不是物理拷贝，实现在jvm中的零拷贝
        CompositeByteBuf byteBuf = Unpooled.compositeBuffer();
        byteBuf.addComponents(header,body);
    }
    @Test
    public void testWrapper(){
        byte[] buf = new byte[1024];
        byte[] buf2 = new byte[1024];
        //共享byte数组的内容，不是拷贝
        ByteBuf byteBuf = Unpooled.wrappedBuffer(buf, buf2);
    }

    @Test
    public void testSlice(){
        byte[] buf = new byte[1024];
        byte[] buf2 = new byte[1024];
        //共享byte数组的内容，不是拷贝
        ByteBuf byteBuf = Unpooled.wrappedBuffer(buf, buf2);

        //同样可以将一个byteBuf，分割成多个，使用共享地址，而非拷贝
        ByteBuf buf3 = byteBuf.slice(1, 5);
        ByteBuf buf4 = byteBuf.slice(6,15);

    }
}
