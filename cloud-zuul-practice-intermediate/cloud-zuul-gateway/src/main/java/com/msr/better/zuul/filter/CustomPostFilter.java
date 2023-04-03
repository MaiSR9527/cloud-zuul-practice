package com.msr.better.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.POST_TYPE;

/**
 * @author MaiShuRen
 * @site https://www.maishuren.top
 * @since 2021-05-16 16:15
 **/
public class CustomPostFilter extends ZuulFilter {

    private static final Logger LOG = LoggerFactory.getLogger(CustomPostFilter.class);

    @Override
    public String filterType() {
        return POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        System.out.println("这是PostFilter！");
        // 从RequestContext获取上下文
        RequestContext requestContext = RequestContext.getCurrentContext();
        // 处理返回中文乱码
        requestContext.getResponse().setCharacterEncoding("UTF-8");
        // 获取上下文中保存的responseBody
        String responseBody = requestContext.getResponseBody();
        // 如果responseBody不为空，则说明流程有异常发生
        if (null != responseBody) {
            //设定返回状态码
            requestContext.setResponseStatusCode(500);
            //替换响应报文
            requestContext.setResponseBody(responseBody);
        }
        return null;
    }
}
