package com.zhukew.auth.domain.service;


import cn.dev33.satoken.stp.SaTokenInfo;
import com.zhukew.auth.domain.entity.AuthUserBO;

import java.util.List;

/**
 * 用户领域service
 *
 * @author: Wei
 * @date: 2023/11/1
 */
public interface AuthUserDomainService {

    /**
     * 注册
     */
    Boolean register(AuthUserBO authUserBO);

    /**
     * 更新用户信息
     */
    Boolean update(AuthUserBO authUserBO);

    /**
     * 更新用户信息
     */
    Boolean delete(AuthUserBO authUserBO);

    /**
     * 用户登录
     */
    SaTokenInfo doLogin(String validCode);

    /**
     * 获取用户信息
     */
    AuthUserBO getUserInfo(AuthUserBO authUserBO);

    /**
     * 批量获取用户信息
     */
    List<AuthUserBO> listUserInfoByIds(List<String> ids);

    /**
     * 用户申请获得上传题目权限
     */
    Boolean subjectAddApplication(AuthUserBO authUserBO);
}

