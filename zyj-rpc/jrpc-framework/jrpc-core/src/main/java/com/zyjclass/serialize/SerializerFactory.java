package com.zyjclass.serialize;

import com.zyjclass.serialize.impl.HessianSerializer;
import com.zyjclass.serialize.impl.JdkSerializer;
import com.zyjclass.serialize.impl.JsonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/23$
 */
@Slf4j
public class SerializerFactory {
    
    private final static ConcurrentHashMap<String,SerializerWrapper> SERIALIZER_MAP = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<Byte,SerializerWrapper> SERIALIZER_MAP_CODE = new ConcurrentHashMap<>();

    static {
        SerializerWrapper jdk = new SerializerWrapper((byte) 1, "jdk", new JdkSerializer());
        SerializerWrapper json = new SerializerWrapper((byte) 2, "json", new JsonSerializer());
        SerializerWrapper hessian = new SerializerWrapper((byte) 3, "hessian", new HessianSerializer());
        SERIALIZER_MAP.put("jdk",jdk);
        SERIALIZER_MAP.put("json",json);
        SERIALIZER_MAP.put("hessian",hessian);
        SERIALIZER_MAP_CODE.put((byte)1,jdk);
        SERIALIZER_MAP_CODE.put((byte)2,json);
        SERIALIZER_MAP_CODE.put((byte)3,hessian);
    }


    /**
     * 使用工厂方法获取一个SerializerWrapper
     * @param serializeType 序列化类型
     * @return 包装类
     */
    public static SerializerWrapper getSerializer(String serializeType) {
        SerializerWrapper serializerWrapper = SERIALIZER_MAP.get(serializeType);
        if (serializerWrapper == null){
            log.error("未找到您指定的序列化策略【{}】，将使用默认序列化策略",serializeType);
            return SERIALIZER_MAP.get("jdk");
        }
        return serializerWrapper;
    }
    /**
     * 使用工厂方法获取一个SerializerWrapper
     * @param serializeCode 序列化类型
     * @return 包装类
     */
    public static SerializerWrapper getSerializer(byte serializeCode) {
        SerializerWrapper serializerWrapper = SERIALIZER_MAP_CODE.get(serializeCode);
        if (serializerWrapper == null){
            log.error("未找到您指定的序列化策略【{}】，将使用默认序列化策略",serializeCode);
            return SERIALIZER_MAP_CODE.get((byte)1);
        }
        return serializerWrapper;
    }
}
