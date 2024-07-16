package com.zhukew.gateway.auth;

import cn.dev33.satoken.stp.StpInterface;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhukew.gateway.entity.AuthPermission;
import com.zhukew.gateway.entity.AuthRole;
import com.zhukew.gateway.redis.RedisUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义权限验证接口扩展
 * 通过该接口，stpUtil 可自主进行权限验证
 *
 * @author: Wei
 * @date: 2023/10/28
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private RedisUtil redisUtil;

    private String authPermissionPrefix = "auth.permission";

    private String authRolePrefix = "auth.role";

    /**
     * 获取用户权限列表
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return getAuth(loginId.toString(), authPermissionPrefix);
    }

    /**
     * 获取用户角色列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return getAuth(loginId.toString(), authRolePrefix);
    }

    /**
     * 封装方法：查询用户权限或角色信息
     */
    private List<String> getAuth(String loginId, String prefix) {
        String authKey = redisUtil.buildKey(prefix, loginId.toString());
        // 从 redis 中查询
        String authValue = redisUtil.get(authKey);
        if (StringUtils.isBlank(authValue)) {
            return Collections.emptyList();
        }
        // 封装 权限 和 角色 list
        List<String> authList = new ArrayList<>();
        if (authRolePrefix.equals(prefix)) {
            List<AuthRole> roleList = new Gson().fromJson(authValue, new TypeToken<List<AuthRole>>() {
            }.getType());
            authList = roleList.stream().map(AuthRole::getRoleKey).collect(Collectors.toList());
        } else if (authPermissionPrefix.equals(prefix)) {
            List<AuthPermission> permissionList = new Gson().fromJson(authValue, new TypeToken<List<AuthPermission>>() {
            }.getType());
            authList = permissionList.stream().map(AuthPermission::getPermissionKey).collect(Collectors.toList());
        }
        return authList;
    }

}
