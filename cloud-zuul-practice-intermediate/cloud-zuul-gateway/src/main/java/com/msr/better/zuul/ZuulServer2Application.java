package com.msr.better.zuul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * @author MaiShuRen
 * @site https://www.maishuren.top
 * @since 2021-05-16 16:15
 **/
@SpringBootApplication
@EnableZuulProxy
@EnableDiscoveryClient
// @EnableOAuth2Sso
public class ZuulServer2Application {
    public static void main(String[] args) {
        SpringApplication.run(ZuulServer2Application.class, args);
    }
}
