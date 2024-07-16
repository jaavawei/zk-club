package com.zhukew.subject.application.interceptor;

import com.zhukew.subject.common.context.LoginContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录拦截器
 *
 * @author: Wei
 * @date: 2023/11/26
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求中取出loginId，并放入线程的上下文对象中
        String loginId = request.getHeader("loginId");
        if (StringUtils.isNotBlank(loginId)) {
            LoginContextHolder.set("loginId", loginId);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        // 请求结束后，将本地记录删除
        LoginContextHolder.remove();
    }

}
