package com.msr.better.zuul.config;

import com.msr.better.zuul.route.DynamicZuulRouteLocator;
import com.msr.better.zuul.service.RoutingRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author MaiShuRen
 * @site <a href="https://www.maishuren.top">maiBlog</a>
 * @since 2023-04-09 20:51
 **/
@Configuration
public class ZuulRouteConfig {

    @Autowired
    private ZuulProperties zuulProperties;

    @Autowired
    private ServerProperties serverProperties;

    @Autowired
    private RoutingRuleService routingRuleService;

    @Bean
    public DynamicZuulRouteLocator routeLocator() {
        DynamicZuulRouteLocator routeLocator = new DynamicZuulRouteLocator(serverProperties.getServlet().getContextPath(),
                zuulProperties, routingRuleService);
        return routeLocator;
    }
}

