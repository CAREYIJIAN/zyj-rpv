package com.zyjclass.compress.impl;

import com.zyjclass.compress.Compressor;
import com.zyjclass.exceptions.CompressException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 使用Gzip进行压缩解压缩
 * @author CAREYIJIAN$
 * @date 2024/1/23$
 */
@Slf4j
public class GzipCompressor implements Compressor {
    @Override
    public byte[] compress(byte[] bytes) {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos)
        ) {
            gzipOutputStream.write(bytes);
            gzipOutputStream.finish();
            byte[] result = baos.toByteArray();
            if (log.isDebugEnabled()){
                log.debug("对字节数组进行了压缩，长度由【{}】压缩至【{}】.",bytes.length,result.length);
            }
            return result;
        } catch (IOException e){
            log.error("对字节数组进行压缩时发生异常",e);
            throw new CompressException(e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        try(ByteArrayInputStream baos = new ByteArrayInputStream(bytes);
            GZIPInputStream gzipInputStream = new GZIPInputStream(baos)
        ) {
            byte[] allBytes = gzipInputStream.readAllBytes();
            if (log.isDebugEnabled()){
                log.debug("对字节数组进行了解压缩，长度由【{}】解压缩至【{}】.",bytes.length,allBytes.length);
            }
            return allBytes;
        } catch (IOException e){
            log.error("对字节数组进行解压缩时发生异常",e);
            throw new CompressException(e);
        }
    }
}
