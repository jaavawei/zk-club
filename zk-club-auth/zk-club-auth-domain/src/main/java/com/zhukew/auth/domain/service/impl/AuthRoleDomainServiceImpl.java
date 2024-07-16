package com.zhukew.auth.domain.service.impl;

import com.zhukew.auth.common.enums.IsDeletedFlagEnum;
import com.zhukew.auth.domain.convert.AuthRoleBOConverter;
import com.zhukew.auth.domain.entity.AuthRoleBO;
import com.zhukew.auth.domain.service.AuthRoleDomainService;
import com.zhukew.auth.infra.basic.entity.AuthRole;
import com.zhukew.auth.infra.basic.service.AuthRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class AuthRoleDomainServiceImpl implements AuthRoleDomainService {

    @Resource
    private AuthRoleService authRoleService;

    /**
     * 增添角色
     */
    @Override
    public Boolean add(AuthRoleBO authRoleBO) {
        AuthRole authRole = AuthRoleBOConverter.INSTANCE.convertBOToEntity(authRoleBO);
        authRole.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        Integer count = authRoleService.insert(authRole);
        return count > 0;
    }

    /**
     * 更新角色
     */
    @Override
    public Boolean update(AuthRoleBO authRoleBO) {
        AuthRole authRole = AuthRoleBOConverter.INSTANCE.convertBOToEntity(authRoleBO);
        Integer count = authRoleService.update(authRole);
        return count > 0;
    }

    /**
     * 删除角色
     */
    @Override
    public Boolean delete(AuthRoleBO authRoleBO) {
        AuthRole authRole = new AuthRole();
        authRole.setId(authRoleBO.getId());
        authRole.setIsDeleted(IsDeletedFlagEnum.DELETED.getCode());
        Integer count = authRoleService.update(authRole);
        return count > 0;
    }

}
