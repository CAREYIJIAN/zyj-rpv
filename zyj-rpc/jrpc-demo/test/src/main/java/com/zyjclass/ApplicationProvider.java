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
public class ApplicationProvider {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationProvider.class,args);
    }

    @GetMapping
    public String Hello(){
        return "hello , provider";
    }
}
