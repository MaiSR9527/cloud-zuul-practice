package com.msr.better.zuul.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

/**
 * @author MaiShuRen
 * @site <a href="https://www.maishuren.top">maiBlog</a>
 * @since 2023=04=02 21:59
 **/
@RestController
@RequestMapping
public class ClientController {

    @GetMapping("/test")
    public String test(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("================header================");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            System.out.println(key + ": " + request.getHeader(key));
        }
        System.out.println("================header================");
        return "hello word!";
    }
}
