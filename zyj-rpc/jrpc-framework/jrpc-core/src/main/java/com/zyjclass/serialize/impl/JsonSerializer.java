package com.zyjclass.serialize.impl;

import com.alibaba.fastjson2.JSON;
import com.zyjclass.exceptions.SerializeException;
import com.zyjclass.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/23$
 */
@Slf4j
public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null){
            return null;
        }
        byte[] bytes = JSON.toJSONBytes(object);
        if (log.isDebugEnabled()){
            log.debug("对象【{}】已经使用JSON完成了序列化操作",object);
        }
        return bytes;
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || clazz == null){
            return null;
        }
        T t = JSON.parseObject(bytes, clazz);

        if (log.isDebugEnabled()){
            log.debug("类【{}】已经使用JSON完成了反序列化操作",clazz);
        }

        return t;
    }
}
