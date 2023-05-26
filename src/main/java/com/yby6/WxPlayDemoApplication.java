// yangbuyi Copyright (c) https://yby6.com 2023.

package com.yby6;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ServletComponentScan
@ComponentScan("com.yby6.*")
public class WxPlayDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(WxPlayDemoApplication.class, args);
    }

}
