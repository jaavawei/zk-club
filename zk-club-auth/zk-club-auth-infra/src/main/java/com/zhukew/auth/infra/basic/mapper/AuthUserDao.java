package com.zhukew.auth.infra.basic.mapper;

import com.zhukew.auth.infra.basic.entity.AuthUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (AuthUser)表数据库访问层
 *
 * @author Wei
 * @since 2023-11-01 01:25:22
 */
public interface AuthUserDao {

    /**
     * 通过ID查询单条数据
     */
    AuthUser queryById(Long id);

    /**
     * 查询指定行数据
     */
    List<AuthUser> queryAllByLimit(AuthUser authUser);

    /**
     * 统计总行数
     */
    long count(AuthUser authUser);

    /**
     * 新增数据
     */
    int insert(AuthUser authUser);

    /**
     * 批量新增数据
     */
    int insertBatch(@Param("entities") List<AuthUser> entities);

    /**
     * 批量新增或按主键更新数据
     */
    int insertOrUpdateBatch(@Param("entities") List<AuthUser> entities);

    /**
     * 修改数据
     */
    int update(AuthUser authUser);

    /**
     * 通过主键删除数据
     */
    int deleteById(Long id);

    /**
     * 更新用户信息
     */
    Integer updateByUserName(AuthUser authUser);

    /**
     * 根据id批量查询
     */
    List<AuthUser> listUserInfoByIds(@Param("userNameList") List<String> userNameList);

    /**
     * 查询用户专项练习次数
     */
    int queryPracticeCount(AuthUser authUser);
}

