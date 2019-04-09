package com.inforefiner.cloud.log;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication

public class LogServer {

    public static void main(String[] args) {
        System.setProperty("spring.config.location", "classpath:logserver.properties");
        SpringApplication.run(com.inforefiner.cloud.log.LogServer.class, args);
    }
}