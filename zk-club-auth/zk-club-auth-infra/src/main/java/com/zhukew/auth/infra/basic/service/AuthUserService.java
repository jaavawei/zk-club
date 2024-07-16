package com.zhukew.auth.infra.basic.service;

import com.zhukew.auth.infra.basic.entity.AuthUser;

import java.util.List;

/**
 * (AuthUser)表服务接口
 *
 * @author Wei
 * @since 2023-11-01 01:25:23
 */
public interface AuthUserService {

    /**
     * 通过ID查询单条数据
     */
    AuthUser queryById(Long id);

    /**
     * 新增数据
     */
    Integer insert(AuthUser authUser);

    /**
     * 修改数据
     */
    Integer update(AuthUser authUser);

    /**
     * 通过主键删除数据
     */
    boolean deleteById(Long id);

    /**
     * 根据条件查询数量
     */
    List<AuthUser> queryByCondition(AuthUser authUser);

    /**
     * 更新用户信息
     */
    Integer updateByUserName(AuthUser authUser);

    /**
     * 根据id批量查询用户信息
     */
    List<AuthUser> listUserInfoByIds(List<String> ids);

    /**
     * 查询用户练习次数
     */
    int queryPracticeCount(AuthUser authUser);
}
