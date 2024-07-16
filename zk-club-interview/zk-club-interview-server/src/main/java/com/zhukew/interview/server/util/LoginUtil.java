package com.zhukew.interview.server.util;

import com.zhukew.interview.server.config.context.LoginContextHolder;

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
