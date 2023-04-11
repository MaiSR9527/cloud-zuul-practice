package com.msr.better.zuul.route;

import com.msr.better.zuul.service.RoutingRuleService;
import org.springframework.cloud.netflix.zuul.filters.RefreshableRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.SimpleRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author MaiShuRen
 * @site <a href="https://www.maishuren.top">maiBlog</a>
 * @since 2023-04-09 20:21
 **/
public class DynamicZuulRouteLocator extends SimpleRouteLocator implements RefreshableRouteLocator {

    private final ZuulProperties zuulProperties;

    private final RoutingRuleService routingRuleService;

    private static final ConcurrentHashMap<String, ZuulRoute> routesCache = new ConcurrentHashMap<>();

    public DynamicZuulRouteLocator(String servletPath, ZuulProperties properties, RoutingRuleService routingRuleService) {
        super(servletPath, properties);
        this.zuulProperties = properties;
        this.routingRuleService = routingRuleService;
    }

    @Override
    public void refresh() {
        doRefresh();
    }

    @Override
    protected Map<String, ZuulProperties.ZuulRoute> locateRoutes() {
        Map<String, ZuulProperties.ZuulRoute> allRoutes = null;
        if (routesCache.isEmpty()) {
            allRoutes = routingRuleService.findAllRoutes();
            routesCache.putAll(allRoutes);
        } else {
            allRoutes = routesCache;
        }
        LinkedHashMap<String, ZuulProperties.ZuulRoute> routesMap = new LinkedHashMap<>();
        routesMap.putAll(super.locateRoutes());
        routesMap.putAll(allRoutes);
        LinkedHashMap<String, ZuulProperties.ZuulRoute> values = new LinkedHashMap<>();
        routesMap.forEach((key, value) -> {
            String path = key;
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            if (StringUtils.hasText(this.zuulProperties.getPrefix())) {
                path = this.zuulProperties.getPrefix() + path;
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }
            }
            values.put(path, value);
        });
        return values;
    }

    public static void clearCache() {
        routesCache.clear();
    }

    public void removeRoute(String id) {
        for (String path : routesCache.keySet()) {
            ZuulRoute zuulRoute = routesCache.get(path);
            if (org.apache.commons.lang3.StringUtils.equals(id, zuulRoute.getId())) {
                routesCache.remove(path);
                // 刷新 SimpleRouteLocator 里的路由信息
                refresh();
            }
        }
    }

    public void addRoute(ZuulRoute zuulRoute) {
        routesCache.put(zuulRoute.getPath(), zuulRoute);
        // 刷新 SimpleRouteLocator 里的路由信息
        refresh();
    }
}
