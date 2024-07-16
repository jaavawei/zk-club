package com.zhukew.auth.domain.service;

import com.zhukew.auth.domain.entity.AuthPermissionBO;

import java.util.List;

/**
 * 角色领域service
 * 
 * @author: Wei
 * @date: 2023/11/1
 */
public interface AuthPermissionDomainService {

    Boolean add(AuthPermissionBO authPermissionBO);

    Boolean update(AuthPermissionBO authPermissionBO);

    Boolean delete(AuthPermissionBO authPermissionBO);

    List<String> getPermission(String userName);
}
