package com.msr.better.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author MaiShuRen
 * @site <a href="https://www.maishuren.top">maiBlog</a>
 * @since 2023-04-09 22:18
 **/
@RestController
@RequestMapping("hello")
public class ServiceBController {

    @GetMapping("hi")
    public Object hi(String name) {
        return "hi " + name + "! This service b";
    }
}
