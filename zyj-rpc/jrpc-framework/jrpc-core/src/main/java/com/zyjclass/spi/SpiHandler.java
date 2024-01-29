package com.zyjclass.spi;

import com.zyjclass.config.ObjectWrapper;
import com.zyjclass.exceptions.SpiException;
import com.zyjclass.loadbalancer.LoadBalancer;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/28$
 */
@Slf4j
public class SpiHandler {
    // 定义一个缓存，用来保存spi相关的原始内容
    private static final Map<String, List<String>> SPI_CONTENT = new ConcurrentHashMap<>(8);

    //定义一个basePath
    private static final String BASE_PATH = "META-INF/jrpc-services";

    //缓存每一个接口所对应的实现的实例
    private static final Map<Class<?>,List<ObjectWrapper<?>>> SPI_IMPLEMENT = new ConcurrentHashMap<>(32);

    //加载当前类之后需要将spi信息保存，避免运行时频繁执行io
    static {
        //加载当前工程和jar包中的classpath中的资源
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        URL fileUrl = contextClassLoader.getResource(BASE_PATH);
        if (fileUrl != null){
            File file = new File(fileUrl.getPath());
            File[] children = file.listFiles();
            if (children != null){
                for (File child : children){
                    String key = child.getName();
                    List<String> value = getImplNames(child);
                    SPI_CONTENT.put(key,value);
                }
            }
        }
    }


    /**
     * 获取一个和当前服务相关的实例
     * @param clazz 一个服务接口的class对象
     * @return
     */
    public synchronized static <T> ObjectWrapper<T> get(Class<?> clazz) {
        //优先走缓存
        List<ObjectWrapper<?>> objectWrappers = SPI_IMPLEMENT.get(clazz);
        if (objectWrappers != null && objectWrappers.size() > 0){
            return (ObjectWrapper<T>)objectWrappers.get(0);
        }

        //构建缓存
        buildCache(clazz);

        //再次尝试获取
        List<ObjectWrapper<?>> result = SPI_IMPLEMENT.get(clazz);
        if (result == null || result.size() == 0){
            return null;
        }
        return (ObjectWrapper<T>) result.get(0);
    }


    /**
     * 获取所有和当前服务相关的实例
     * @param clazz 一个服务接口的class对象
     * @return
     * @param <T>
     */
    public synchronized static <T> List<ObjectWrapper<T>> getList(Class<?> clazz) {
        //优先走缓存
        List<ObjectWrapper<?>> objectWrappers = SPI_IMPLEMENT.get(clazz);
        if (objectWrappers != null && objectWrappers.size() > 0) {
            return objectWrappers.stream().map(objectWrapper -> (ObjectWrapper<T>) objectWrapper).collect(Collectors.toList());
        }

        //构建缓存
        buildCache(clazz);

        //再此缓存
        objectWrappers = SPI_IMPLEMENT.get(clazz);
        if (objectWrappers != null && objectWrappers.size() > 0) {
            return objectWrappers.stream().map(objectWrapper -> (ObjectWrapper<T>) objectWrapper).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * 构建clazz相关的缓存
     * @param clazz 某个类的class实例
     */
    private static void buildCache(Class<?> clazz) {
        //通过clazz获取与之匹配的实现名称
        String name = clazz.getName();
        List<String> implNames = SPI_CONTENT.get(name);
        if (implNames == null || implNames.size() == 0){
            return;
        }

        //实例化所有实现
        List<ObjectWrapper<?>> impls = new ArrayList<>();
        for (String implName : implNames){
            try {
                //首先进行分割
                String[] codeAndTypeAndName = implName.split("-");
                if (codeAndTypeAndName.length != 3){
                    throw new SpiException("您配置的spi文件不合法");
                }
                byte code = Byte.valueOf(codeAndTypeAndName[0]);
                String type = codeAndTypeAndName[1];
                String implementName = codeAndTypeAndName[2];

                Class<?> aClass = Class.forName(implementName);
                Object impl = aClass.getConstructor().newInstance();

                ObjectWrapper<Object> objectWrapper = new ObjectWrapper<>(code, type, impl);

                impls.add(objectWrapper);
            }catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
                log.error("实例化【{}】的spi实现发生了异常",implName,e);
            }
        }
        SPI_IMPLEMENT.put(clazz,impls);
    }

    /**
     * 获取文件所有的实现名称
     * @param child 文件对象
     * @return      实现类的权限定名称集合
     */
    private static List<String> getImplNames(File child) {
        try (FileReader fileReader = new FileReader(child);
             //用装饰器设计模式包装一下，操作（api）可能会更方便
             BufferedReader bufferedReader = new BufferedReader(fileReader);
        ){
            ArrayList<String> implNames = new ArrayList<>();
            while (true){
                String line = bufferedReader.readLine();
                if (line == null || "".equals(line)) break;
                implNames.add(line);
            }
            return implNames;
        }catch (IOException e){
            log.error("读取spi文件时发生异常",e);
        }
        return null;
    }
}
