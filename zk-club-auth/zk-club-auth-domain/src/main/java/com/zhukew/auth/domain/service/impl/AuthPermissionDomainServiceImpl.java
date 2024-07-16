package com.zhukew.auth.domain.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhukew.auth.common.enums.IsDeletedFlagEnum;
import com.zhukew.auth.domain.convert.AuthPermissionBOConverter;
import com.zhukew.auth.domain.entity.AuthPermissionBO;
import com.zhukew.auth.domain.redis.RedisUtil;
import com.zhukew.auth.domain.service.AuthPermissionDomainService;
import com.zhukew.auth.infra.basic.entity.AuthPermission;
import com.zhukew.auth.infra.basic.service.AuthPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthPermissionDomainServiceImpl implements AuthPermissionDomainService {

    @Resource
    private AuthPermissionService authPermissionService;

    @Resource
    private RedisUtil redisUtil;

    private String authPermissionPrefix = "auth.permission";

    /**
     * 增添权限信息
     */
    @Override
    public Boolean add(AuthPermissionBO authPermissionBO) {
        AuthPermission authPermission = AuthPermissionBOConverter.INSTANCE.convertBOToEntity(authPermissionBO);
        authPermission.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        Integer count = authPermissionService.insert(authPermission);
        return count > 0;
    }

    /**
     * 更新权限信息
     */
    @Override
    public Boolean update(AuthPermissionBO authPermissionBO) {
        AuthPermission authPermission = AuthPermissionBOConverter.INSTANCE.convertBOToEntity(authPermissionBO);
        Integer count = authPermissionService.update(authPermission);
        return count > 0;
    }

    /**
     * 删除用户权限
     */
    @Override
    public Boolean delete(AuthPermissionBO authPermissionBO) {
        AuthPermission authPermission = new AuthPermission();
        authPermission.setId(authPermissionBO.getId());
        authPermission.setIsDeleted(IsDeletedFlagEnum.DELETED.getCode());
        Integer count = authPermissionService.update(authPermission);
        return count > 0;
    }

    /**
     * 查询用户权限
     */
    @Override
    public List<String> getPermission(String userName) {
        // 用权限前缀和用户名组装成 key，并从 redis 中查询用户拥有的权限信息
        String permissionKey = redisUtil.buildKey(authPermissionPrefix, userName);
        String permissionValue = redisUtil.get(permissionKey);
        if (StringUtils.isBlank(permissionValue)) {
            return Collections.emptyList();
        }
        List<AuthPermission> permissionList = new Gson().fromJson(permissionValue,
                new TypeToken<List<AuthPermission>>() {
                }.getType());
        List<String> authList = permissionList.stream().map(AuthPermission::getPermissionKey).collect(Collectors.toList());
        return authList;
    }

}
