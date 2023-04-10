package com.msr.better.zuul.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author MaiShuRen
 * @site <a href="https://www.maishuren.top">maiBlog</a>
 * @date 2023-04-10 16:18:11
 */
public class RefreshRouteEvent extends ApplicationEvent {

    public RefreshRouteEvent(Object source) {
        super(source);
    }
}
