package com.zhukew.circle.server.config.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * Feign请求拦截器
 *
 * @author: Wei
 * @date: 2023/12/3
 */
@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        // 拿到原始请求
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        // 将 loginId 放入新请求中
        if (Objects.nonNull(request)) {
            String loginId = request.getHeader("loginId");
            if (StringUtils.isNotBlank(loginId)) {
                requestTemplate.header("loginId", loginId);
            }
        }
    }

}
