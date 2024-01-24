package com.zyjclass.compress;

import com.zyjclass.compress.impl.GzipCompressor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/23$
 */
@Slf4j
public class CompressorFactory {

    private final static ConcurrentHashMap<String, CompressorWrapper> COMPRESSOR_MAP = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<Byte, CompressorWrapper> COMPRESSOR_MAP_CODE = new ConcurrentHashMap<>();

    static {
        CompressorWrapper gzip = new CompressorWrapper((byte) 1, "gzip", new GzipCompressor());
        COMPRESSOR_MAP.put("gzip",gzip);
        COMPRESSOR_MAP_CODE.put((byte)1,gzip);
    }


    /**
     * 使用工厂方法获取一个CompressorWrapper
     * @param compressorType 压缩类型
     * @return 包装类
     */
    public static CompressorWrapper getCompressor(String compressorType) {
        CompressorWrapper compressorWrapper = COMPRESSOR_MAP.get(compressorType);
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
    public static CompressorWrapper getCompressor(byte compressorCode) {
        CompressorWrapper compressorWrapper = COMPRESSOR_MAP_CODE.get(compressorCode);
        if (compressorWrapper == null){
            log.error("未找到您指定的压缩算法【{}】，将使用默认算法",compressorCode);
            return COMPRESSOR_MAP_CODE.get((byte)1);
        }
        return compressorWrapper;
    }
}
