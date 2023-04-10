package com.msr.better.zuul;

import com.msr.better.zuul.entity.RoutingRule;
import com.msr.better.zuul.service.RoutingRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author MaiShuRen
 * @site <a href="https://www.maishuren.top">maiBlog</a>
 * @date 2023-04-10 15:22:17
 */
@RestController
@RequestMapping("route")
public class RoutingRuleController {

    @Autowired
    private RoutingRuleService routingRuleService;

    @PostMapping("add")
    public Object addRoute(@RequestBody RoutingRule routingRule) {
        routingRuleService.save(routingRule);
        return "success";
    }

    @PostMapping("update")
    public Object updateRoute(@RequestBody RoutingRule routingRule) {
        routingRuleService.save(routingRule);
        return "success";
    }

    @DeleteMapping("delete")
    public Object updateRoute(Long id) {
        routingRuleService.delete(id);
        return "success";
    }
}
