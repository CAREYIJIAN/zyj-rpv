package com.zyjclass.serialize;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/23$
 */
@Slf4j
public class SerializeUtil {

    public static byte[] serialize(Object object){
        //心跳的请求没有payload
        if (object == null){
            return null;
        }
        //对象怎么变成一个字节数据 序列化 压缩
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(baos);) {
            outputStream.writeObject(object);
            //压缩

            return baos.toByteArray();
        } catch (IOException e) {
            log.error("序列化时出现异常");
            throw new RuntimeException(e);
        }
    }


}
