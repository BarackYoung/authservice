package com.atom.authservice;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class BootstarpApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootstarpApplication.class, args);
    }

}
