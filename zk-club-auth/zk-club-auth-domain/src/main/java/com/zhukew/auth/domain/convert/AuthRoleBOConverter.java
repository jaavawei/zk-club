package com.zhukew.auth.domain.convert;

import com.zhukew.auth.domain.entity.AuthRoleBO;
import com.zhukew.auth.infra.basic.entity.AuthRole;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 角色bo转换器
 * 
 * @author: Wei
 * @date: 2023/10/8
 */
@Mapper
public interface AuthRoleBOConverter {

    AuthRoleBOConverter INSTANCE = Mappers.getMapper(AuthRoleBOConverter.class);

    AuthRole convertBOToEntity(AuthRoleBO authRoleBO);

}
