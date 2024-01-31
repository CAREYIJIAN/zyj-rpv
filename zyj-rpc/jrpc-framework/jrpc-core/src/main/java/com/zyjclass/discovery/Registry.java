package com.zyjclass.discovery;

import com.zyjclass.ServiceConfig;

import java.net.InetSocketAddress;
import java.util.List;

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
     * 从注册中心拉取服务列表
     * @param serviceName 服务的名称
     * @return 服务列表
     */
    List<InetSocketAddress> lookUp(String serviceName, String group);

}
