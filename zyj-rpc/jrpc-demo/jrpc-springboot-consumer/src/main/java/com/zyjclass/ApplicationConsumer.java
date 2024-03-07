package com.zyjclass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author CAREYIJIAN$
 * @date 2024/1/31$
 */
@SpringBootApplication
@RestController
public class ApplicationConsumer {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationConsumer.class,args);
    }

    @GetMapping("test")
    public String Hello(){
        return "hello , consumer";
    }
}
