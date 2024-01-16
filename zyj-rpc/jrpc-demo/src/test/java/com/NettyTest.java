package com;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;
import zyjrpcclass.netty.AppClient;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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

    //设计报文（协议结构）
    @Test
    public void testMessage() throws IOException{
        ByteBuf message = Unpooled.buffer();
        //示例数据
        message.writeBytes("ydl".getBytes(StandardCharsets.UTF_8));
        message.writeByte(1);
        message.writeShort(125);
        message.writeInt(256);
        message.writeByte(1);
        message.writeByte(0);
        message.writeByte(2);
        message.writeLong(252333L);
        //用对象流转化为字节数据(示例对象)
        AppClient appClient = new AppClient();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        oos.writeObject(appClient);

        byte[] bytes = outputStream.toByteArray();
        message.writeBytes(bytes);

        //将message转为字节输出
        //printAsBinary(message);
    }
    /*public static void printAsBinary(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(byteBuf.readerIndex(), bytes);
        String binaryString = ByteBufUtil.hexDump(bytes);
        StringBuilder formattedBinary = new StringBuilder();
        for (int i = 0; i < binaryString.length(); i += 2) {
            formattedBinary.append(binaryString.substring(i, i + 2)).append(" ");
        }
        System.out.println("Binary representation: " + formattedBinary.toString());
    }*/


    //测试压缩
    @Test
    public void testCompress() throws IOException{
        byte[] buf = new byte[]{12,3,12,34,45,6,7,78,5,43};

        //本质就是，将buf作为输入，将结果输出到另一个字节数组当中
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos);

        gzipOutputStream.write(buf);
        gzipOutputStream.finish();

        byte[] bytes = baos.toByteArray();
        System.out.println(Arrays.toString(bytes));
    }


}
