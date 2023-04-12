package com.msr.better.usercenter.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author MaiShuRen
 * @site <a href="https://www.maishuren.top">maiBlog</a>
 * @since 2023-04-12 10:44:57
 */
@RestController
@RequestMapping("user")
public class UsercenterController {

    @Value("${spring.profiles}")
    private String profile;
    @Value("${server.port}")
    private Integer port;

    @GetMapping("list")
    public String list() {
        return "active " + profile + " port " + port;
    }
}
