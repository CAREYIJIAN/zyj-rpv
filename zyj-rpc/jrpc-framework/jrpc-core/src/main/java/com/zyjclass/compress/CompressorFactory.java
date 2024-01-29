package com.zyjclass.compress;

import com.zyjclass.compress.impl.GzipCompressor;
import com.zyjclass.config.ObjectWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/23$
 */
@Slf4j
public class CompressorFactory {

    private final static Map<String, ObjectWrapper<Compressor>> COMPRESSOR_MAP = new ConcurrentHashMap<>();
    private final static Map<Byte, ObjectWrapper<Compressor>> COMPRESSOR_MAP_CODE = new ConcurrentHashMap<>();

    static {
        ObjectWrapper<Compressor> gzip = new ObjectWrapper((byte) 1, "gzip", new GzipCompressor());
        COMPRESSOR_MAP.put("gzip",gzip);
        COMPRESSOR_MAP_CODE.put((byte)1,gzip);
    }


    /**
     * 使用工厂方法获取一个CompressorWrapper
     * @param compressorType 压缩类型
     * @return 包装类
     */
    public static ObjectWrapper<Compressor> getCompressor(String compressorType) {
        ObjectWrapper<Compressor> compressorWrapper = COMPRESSOR_MAP.get(compressorType);
        if (compressorWrapper == null){
            log.error("未找到您指定的压缩算法【{}】，将使用默认算法",compressorType);
            return COMPRESSOR_MAP.get("gzip");
        }
        return compressorWrapper;
    }
    /**
     * 使用工厂方法获取一个CompressorWrapper
     * @param compressorCode 压缩类型
     * @return 包装类
     */
    public static ObjectWrapper<Compressor> getCompressor(byte compressorCode) {
        ObjectWrapper<Compressor> compressorWrapper = COMPRESSOR_MAP_CODE.get(compressorCode);
        if (compressorWrapper == null){
            log.error("未找到您指定的压缩算法【{}】，将使用默认算法",compressorCode);
            return COMPRESSOR_MAP_CODE.get((byte)1);
        }
        return compressorWrapper;
    }

    /**
     * 添加一个新的压缩策略
     * @param compressorWrapper  具体的实现的包装类
     */
    public static void addCompressor(ObjectWrapper<Compressor> compressorWrapper){
        String type = compressorWrapper.getType();
        byte code = compressorWrapper.getCode();
        COMPRESSOR_MAP.put(type, compressorWrapper);
        COMPRESSOR_MAP_CODE.put(code, compressorWrapper);
    }

}
