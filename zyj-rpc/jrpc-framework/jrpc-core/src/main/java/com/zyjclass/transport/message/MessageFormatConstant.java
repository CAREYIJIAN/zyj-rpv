package com.zyjclass.transport.message;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/20$
 */
public class MessageFormatConstant {
    public final static byte[] MAGIC = "jrpc".getBytes();
    public final static byte VERSION = 1;
    //头部信息的长度
    public final static short HEADER_LENGTH =(byte)(MAGIC.length + 1 + 2 + 4 + 1 + 1 + 1 + 8 + 8);
    public final static short FULL_LENGTH = 4;
    public final static int MAX_FRAME_LENGTH = 1024 * 1024; //最大帧长度

    public static int VERSION_LENGTH = 1;
    //头部信息的长度占用的字节数
    public static int HEADER_FIELD_LENGTH = 2;
    public static int FULL_FIELD_LENGTH = 4;
}
