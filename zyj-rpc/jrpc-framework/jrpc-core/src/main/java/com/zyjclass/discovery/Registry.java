package com.zyjclass.discovery;

import com.zyjclass.ServiceConfig;

import java.net.InetSocketAddress;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/18$
 */
public interface Registry {

    /**
     * 注册服务
     * @param serviceConfig 服务配置内容
     */
    public void register(ServiceConfig<?> serviceConfig);


    /**
     * 从注册中心拉取一个可用的服务
     * @param serviceName 服务的名称
     * @return 服务的IP和端口（地址）
     */
    InetSocketAddress lookUp(String serviceName);

}
