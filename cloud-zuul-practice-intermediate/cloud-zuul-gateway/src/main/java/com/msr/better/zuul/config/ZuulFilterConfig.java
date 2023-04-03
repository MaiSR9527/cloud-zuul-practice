package com.msr.better.zuul.config;

import com.msr.better.zuul.filter.CustomPostFilter;
import com.msr.better.zuul.filter.CustomPreFilter;
import com.msr.better.zuul.filter.FirstPreFilter;
import com.msr.better.zuul.filter.SecondPreFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author MaiShuRen
 * @site https://www.maishuren.top
 * @since 2021-05-16 16:15
 **/
// @Configuration
public class ZuulFilterConfig {

    @Bean
    public FirstPreFilter firstPreFilter() {
        return new FirstPreFilter();
    }

    @Bean
    public SecondPreFilter secondPreFilter() {
        return new SecondPreFilter();
    }

    @Bean
    public CustomPostFilter customPostFilter() {
        return new CustomPostFilter();
    }

    @Bean
    public CustomPreFilter customPreFilter() {
        return new CustomPreFilter();
    }
}
