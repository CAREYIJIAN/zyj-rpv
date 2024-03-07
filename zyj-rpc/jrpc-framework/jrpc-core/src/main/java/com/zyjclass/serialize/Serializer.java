package com.zyjclass.serialize;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/23$
 */
public interface Serializer {

    /**
     * 抽象的用来做，序列化的方法
     * @param object 带序列化的对象实例
     * @return 字节数组
     */
    byte[] serialize(Object object);

    /**
     * 抽象的用来做，反序列化的方法
     * @param bytes 带反序列化的字节数组
     * @param clazz 目标类的class对象
     * @return 目标实例
     * @param <T> 目标类泛型
     */

    <T> T deserialize(byte[] bytes, Class<T> clazz);


}
