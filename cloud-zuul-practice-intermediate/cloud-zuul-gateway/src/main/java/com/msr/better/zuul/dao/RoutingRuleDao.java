package com.msr.better.zuul.dao;

import com.msr.better.zuul.entity.RoutingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author MaiShuRen
 * @site <a href="https://www.maishuren.top">maiBlog</a>
 * @since 2023-04-09 20:10
 **/
@Repository
public interface RoutingRuleDao extends JpaRepository<RoutingRule, Long> {
}
