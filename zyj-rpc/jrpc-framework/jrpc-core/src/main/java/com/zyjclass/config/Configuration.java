package com.zyjclass.config;


import com.zyjclass.IdGenerator;
import com.zyjclass.discovery.RegistryConfig;
import com.zyjclass.loadbalancer.LoadBalancer;
import com.zyjclass.loadbalancer.impl.RoundRobinLoadBalancer;
import com.zyjclass.protection.RateLimiter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 全局的配置类，代码配置 ———》xml配置———》spi配置———》默认项
 * @author CAREYIJIAN$
 * @date 2024/1/27$
 */
@Data
@Slf4j
public class Configuration {
    //端口号（配置信息）
    private int port = 8090;
    //序列化协议（配置信息）
    private String serializeType = "jdk";
    //压缩使用的协议（配置信息）
    private String compressType = "gzip";
    //应用程序的名字（配置信息）
    private String appName = "default";
    //配置的注册中心（配置信息）
    private RegistryConfig registryConfig = new RegistryConfig("zookeeper://127.0.0.1:2181");
    //id生成器（配置信息）
    private IdGenerator idGenerator = new IdGenerator(1,2);
    //负载均衡策略（配置信息）
    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();
    //限流器,为每一个ip（配置信息）
    private Map<SocketAddress, RateLimiter> everyIpRateLimiter = new ConcurrentHashMap<>();

    //读xml
    public Configuration(){
        //1、首先是成员变量的默认配置项


        //2、spi机制发现相关配置项
        SpiResolver spiResolver = new SpiResolver();
        spiResolver.loadFromSpi(this);

        //3、读取xml获得上边的信息
        XmlResolver xmlResolver = new XmlResolver();
        xmlResolver.loadFromXml(this);

        //4、编程配置项，jrpcBootstrap提供
    }

}
