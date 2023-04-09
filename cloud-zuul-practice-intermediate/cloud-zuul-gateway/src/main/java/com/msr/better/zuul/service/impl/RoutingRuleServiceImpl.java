package com.msr.better.zuul.service.impl;

import com.msr.better.zuul.dao.RoutingRuleDao;
import com.msr.better.zuul.entity.RoutingRule;
import com.msr.better.zuul.service.RoutingRuleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author MaiShuRen
 * @site <a href="https://www.maishuren.top">maiBlog</a>
 * @since 2023-04-09 20:23
 **/
@Service
public class RoutingRuleServiceImpl implements RoutingRuleService {

    @Autowired
    private RoutingRuleDao routingRuleDao;

    @Override
    public Map<String, ZuulProperties.ZuulRoute> findAllRoutes() {
        RoutingRule routingRule = new RoutingRule();
        routingRule.setEnabled(1);
        Example<RoutingRule> example = Example.of(routingRule);
        List<RoutingRule> routingRuleList = routingRuleDao.findAll(example);

        Map<String, ZuulProperties.ZuulRoute> zuulRouteMap = new LinkedHashMap<>();

        routingRuleList.stream().filter(item -> StringUtils.isNotBlank(item.getPath()))
                .forEach(item -> {
                    ZuulProperties.ZuulRoute zuulRoute = new ZuulProperties.ZuulRoute();
                    BeanUtils.copyProperties(item, zuulRoute);
                    zuulRoute.setId(String.valueOf(item.getId()));
                    zuulRouteMap.put(item.getPath(), zuulRoute);
                });
        return zuulRouteMap;
    }
}
