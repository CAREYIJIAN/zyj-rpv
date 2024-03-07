package com.zyjclass.impl;

import com.zyjclass.HelloJrpc2;
import com.zyjclass.annotation.JrpcApi;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/17$
 */
@JrpcApi
public class HelloJrpcImpl2 implements HelloJrpc2 {
    @Override
    public String sayHi(String msg) {
        return "hi consumer:" + msg;
    }
}
