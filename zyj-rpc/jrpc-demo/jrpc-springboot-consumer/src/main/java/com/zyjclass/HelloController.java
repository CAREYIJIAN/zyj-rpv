package com.zyjclass;

import com.zyjclass.annotation.JrpcService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/31$
 */
@RestController
public class HelloController {

    //需要注入一个代理对象
    @JrpcService
    private HelloJrpc helloJrpc;

    @GetMapping("hello")
    public String hello(){
        return helloJrpc.sayHi("你看我调用成不成功就完了！灵魂之汁，浇给~~~");
    }
}
