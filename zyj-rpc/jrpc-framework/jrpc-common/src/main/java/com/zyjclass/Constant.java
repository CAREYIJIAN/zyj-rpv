package com.zyjclass;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/17$
 */
public class Constant {
    // 默认Zookeeper的连接地址
    public static final String Default_ZK_CONNECT ="127.0.0.1:2181";
    // 默认Zookeeper的超时时间
    public static final int TIME_OUT = 10000;

    //服务提供方和调用方在注册中心的基础路径
    public static final String BASE_PROVIDERS_PATH = "/jrpc-metdata/providers";
    public static final String BASE_CONSUMERS_PATH = "/jrpc-metdata/consumers";
}
