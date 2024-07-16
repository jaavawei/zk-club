package com.zhukew.circle.server.util;

import com.zhukew.circle.server.config.context.LoginContextHolder;

/**
 * 用户登录util
 *
 * @author: Wei
 * @date: 2023/11/26
 */
public class LoginUtil {

    public static String getLoginId() {
        return LoginContextHolder.getLoginId();
    }


}
