package com.zyjclass.config;

import com.zyjclass.compress.Compressor;
import com.zyjclass.compress.CompressorFactory;
import com.zyjclass.loadbalancer.LoadBalancer;
import com.zyjclass.serialize.Serializer;
import com.zyjclass.serialize.SerializerFactory;
import com.zyjclass.spi.SpiHandler;

import java.util.List;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/28$
 */
public class SpiResolver {
    /**
     * 通过spi的方式加载配置项
     * @param configuration 配置上下文
     */
    public void loadFromSpi(Configuration configuration) {
        // 我的spi的文件中配置了很多实现（自由定义、只能配置一个实现、配置多个）

        List<ObjectWrapper<LoadBalancer>> loadBalancerWrappers = SpiHandler.getList(LoadBalancer.class);
        //将其放入工厂
        if (loadBalancerWrappers != null && loadBalancerWrappers.size() > 0){
            configuration.setLoadBalancer(loadBalancerWrappers.get(0).getImpl());
        }

        List<ObjectWrapper<Compressor>> compressorWrappers = SpiHandler.getList(Compressor.class);
        if (compressorWrappers != null){
            compressorWrappers.forEach(objectWrapper -> {
                CompressorFactory.addCompressor(objectWrapper);
            });
        }

        List<ObjectWrapper<Serializer>> serializerWrappers = SpiHandler.getList(Serializer.class);
        if (serializerWrappers != null){
            serializerWrappers.forEach(objectWrapper -> {
                SerializerFactory.addSerializer(objectWrapper);
            });
        }


    }
}
