package com.zhukew.wx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 微信服务启动器
 * 
 * @author: Wei
 * @date: 2023/10/11
 */
@SpringBootApplication
@ComponentScan("com.zhukew")
public class WxApplication {

    public static void main(String[] args) {
        SpringApplication.run(WxApplication.class);
    }

}