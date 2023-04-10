package com.msr.better.zuul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

/**
 * @author MaiShuRen
 * @site https://www.maishuren.top
 * @since 2021-05-16 15:53
 **/
@SpringBootApplication
@EnableDiscoveryClient
@EnableResourceServer
public class ServiceA2Application {

    public static void main(String[] args) {
        SpringApplication.run(ServiceA2Application.class, args);
    }
}
