package com.msr.better.zuul.service;

import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;

import java.util.Map;

/**
 * @author MaiShuRen
 * @site <a href="https://www.maishuren.top">maiBlog</a>
 * @since 2023-04-09 20:23
 **/
public interface RoutingRuleService {

    Map<String, ZuulProperties.ZuulRoute> findAllRoutes();
}
