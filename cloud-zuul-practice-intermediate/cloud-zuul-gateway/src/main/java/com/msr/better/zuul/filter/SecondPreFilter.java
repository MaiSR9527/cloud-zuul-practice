package com.msr.better.zuul.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * @author MaiShuRen
 * @site https://www.maishuren.top
 * @since 2021-05-16 16:15
 **/
public class SecondPreFilter extends ZuulFilter {
    private Logger log = LoggerFactory.getLogger(SecondPreFilter.class);

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 3;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext requestContext = RequestContext.getCurrentContext();
        // 参数a是否检验成功，不成功那就没必要继续执行下去
        return (boolean) requestContext.get("parameter-check-success");
    }

    @Override
    public Object run() throws ZuulException {
        log.info("second pre filter...");
        // 拿到请求上下文
        RequestContext requestContext = RequestContext.getCurrentContext();
        // 拿到HttpServletRequest
        HttpServletRequest request = requestContext.getRequest();
        // 获取传入的参数值
        String b = request.getParameter("b");
        if (StringUtils.isBlank(b)) {
            // 禁止路由，也就是不允许访问下游服务
            requestContext.setSendZuulResponse(false);
            // 设置响应结果，供PostFilter使用，参数是字符串，序列化一下返回对象也行。
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map = new HashMap<>();
            map.put("code", -1);
            map.put("msg", "参数b不能为空");
            String result = null;
            try {
                result = mapper.writeValueAsString(map);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            requestContext.setResponseBody(result);
            // parameter-check-success保存于上下文，作为同类型下游Filter的执行开关
            requestContext.set("parameter-check-success", false);
            return null;
        }
        return null;
    }
}
