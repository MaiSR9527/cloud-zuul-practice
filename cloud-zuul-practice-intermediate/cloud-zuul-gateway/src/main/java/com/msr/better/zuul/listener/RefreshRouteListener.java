package com.msr.better.zuul.listener;

import com.msr.better.zuul.event.RefreshRouteEvent;
import com.msr.better.zuul.route.DynamicZuulRouteLocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.RoutesRefreshedEvent;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author MaiShuRen
 * @site <a href="https://www.maishuren.top">maiBlog</a>
 * @date 2023-04-10 16:16:29
 */
@Component
public class RefreshRouteListener implements ApplicationListener<ApplicationEvent> {

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private RouteLocator routeLocator;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof RefreshRouteEvent) {
            DynamicZuulRouteLocator.clearCache();
            publisher.publishEvent(new RoutesRefreshedEvent(routeLocator));
        }
    }


}
