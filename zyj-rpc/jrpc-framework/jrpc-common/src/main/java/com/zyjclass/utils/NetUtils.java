package com.zyjclass.utils;

import com.zyjclass.exceptions.NetworkException;
import lombok.extern.slf4j.Slf4j;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/18$
 */
@Slf4j
public class NetUtils {

    /**
     * 获取ip地址
     * @return
     */
    public static String getIp(){
        try {
            //获取所有的网卡信息
            Enumeration<NetworkInterface> interfaces =  NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()){
                NetworkInterface iface = interfaces.nextElement();
                //过滤非回环接口和虚拟接口
                if (iface.isLoopback() || iface.isVirtual() || !iface.isUp()){
                    continue;
                }
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()){
                    InetAddress addr = addresses.nextElement();
                    //过滤TPV6地址和回环地址
                    if (addr instanceof Inet6Address || addr.isLoopbackAddress()){
                        continue;
                    }
                    String ipAddress = addr.getHostAddress();
                    if (log.isDebugEnabled()){
                        log.debug("局域网IP地址：{}",ipAddress);
                    }
                    return ipAddress;
                }
            }
            throw new NetworkException();
        }catch (SocketException ex){
            log.error("获取局域网ip时发生异常：",ex);
            throw new NetworkException(ex);
        }
    }

    public static void main(String[] args) {
        String ip = NetUtils.getIp();
        System.out.println(ip);
    }

}
