package com.zyjclass.serialize.impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.zyjclass.exceptions.SerializeException;
import com.zyjclass.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/23$
 */
@Slf4j
public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null){
            return null;
        }
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Hessian2Output hessian2Output = new Hessian2Output(baos);
            hessian2Output.writeObject(object);
            hessian2Output.flush();
            if (log.isDebugEnabled()){
                log.debug("对象【{}】已经使用Hessian完成了序列化操作",object);
            }
            return baos.toByteArray();
        }catch (IOException e){
            log.error("使用Hessian序列化对象【{}】时发生异常",object);
            throw new SerializeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || clazz == null){
            return null;
        }
        try(ByteArrayInputStream bais = new ByteArrayInputStream(bytes);) {
            Hessian2Input hessian2Input = new Hessian2Input(bais);
            Object object = hessian2Input.readObject();
            if (log.isDebugEnabled()){
                log.debug("类【{}】已经使用Hessian完成了反序列化操作",clazz);
            }
            return (T)object;
        }catch (IOException e){
            log.error("使用Hessian反序列化【{}】时发生异常",clazz);
            throw new SerializeException(e);
        }
    }
}
