package com.zyjclass.impl;

import com.zyjclass.HelloJrpc;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/17$
 */
public class HelloJrpcImpl implements HelloJrpc {
    @Override
    public String sayHi(String msg) {
        return "hi consumer:" + msg;
    }
}
