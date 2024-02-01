package com.zyjclass.impl;

import com.zyjclass.HelloJrpc;
import com.zyjclass.annotation.JrpcApi;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/17$
 */
@JrpcApi(group = "primary")
public class HelloJrpcImpl implements HelloJrpc {
    @Override
    public String sayHi(String msg) {
        return "hi consumer:" + msg;
    }
}
