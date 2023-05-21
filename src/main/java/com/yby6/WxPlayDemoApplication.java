package com.yby6;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WxPlayDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(WxPlayDemoApplication.class, args);
    }

}
