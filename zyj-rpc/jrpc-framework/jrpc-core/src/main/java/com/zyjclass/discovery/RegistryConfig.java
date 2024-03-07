package com.zyjclass.discovery;

import com.zyjclass.Constant;
import com.zyjclass.discovery.Registry;
import com.zyjclass.discovery.impl.NacosRegistry;
import com.zyjclass.discovery.impl.ZookeeperRegistry;
import com.zyjclass.exceptions.DiscoveryException;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/17$
 */
public class RegistryConfig {

    //定义连接的url zookeeper://127.0.0.1:2181  redis://192.168.142:3306
    private final String connectString;

    public RegistryConfig(String connectString){
        this.connectString = connectString;
    }

    /**
     * 可以用简单工厂来实现,返回注册中心实例
     * @return
     */
    public Registry getRegistry() {
        //1.获取注册中心的类型
        String registryType = getRegistryType(connectString,true);
        if(registryType.equals("zookeeper")){
            String host = getRegistryType(connectString,false);
            return new ZookeeperRegistry(host, Constant.TIME_OUT);
        }else if(registryType.equals("nacos")){
            String host = getRegistryType(connectString,false);
            return new NacosRegistry(host, Constant.TIME_OUT);
        }
        throw new DiscoveryException("未发现合适的注册中心");
    }

    private String getRegistryType(String connectString, Boolean isType){
        String[] typeAndHost = connectString.split("://");
        if (typeAndHost.length != 2){
            throw new RuntimeException("给定的注册中心连接url不合法");
        }
        if (isType){
            return typeAndHost[0];
        }else {
            return typeAndHost[1];
        }
    }

}
