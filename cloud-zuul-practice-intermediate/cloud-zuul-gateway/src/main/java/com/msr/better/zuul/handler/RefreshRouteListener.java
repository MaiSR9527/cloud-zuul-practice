package com.msr.better.zuul.handler;

import com.msr.better.zuul.entity.RoutingRule;
import com.msr.better.zuul.event.RefreshRouteEvent;
import com.msr.better.zuul.route.DynamicZuulRouteLocator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;
import org.springframework.context.ApplicationEvent;
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
    private DynamicZuulRouteLocator dynamicZuulRouteLocator;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof RefreshRouteEvent) {
            // 全量加载路由信息
            //DynamicZuulRouteLocator.clearCache();
            //publisher.publishEvent(new RoutesRefreshedEvent(routeLocator));
            // 增量修改路由信息
            RefreshRouteEvent refreshRouteEvent = (RefreshRouteEvent) event;
            RoutingRule routingRule = (RoutingRule) event.getSource();
            if (refreshRouteEvent.isDelete()) {
                dynamicZuulRouteLocator.removeRoute(String.valueOf(routingRule.getId()));
            } else {
                ZuulRoute zuulRoute = new ZuulRoute();
                BeanUtils.copyProperties(routingRule, zuulRoute);
                zuulRoute.setId(String.valueOf(routingRule.getId()));
                dynamicZuulRouteLocator.addRoute(zuulRoute);
            }
        }
    }
}
