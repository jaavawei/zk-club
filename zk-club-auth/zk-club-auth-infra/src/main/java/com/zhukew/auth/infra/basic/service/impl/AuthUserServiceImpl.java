package com.zhukew.auth.infra.basic.service.impl;

import com.zhukew.auth.infra.basic.entity.AuthUser;
import com.zhukew.auth.infra.basic.mapper.AuthUserDao;
import com.zhukew.auth.infra.basic.service.AuthUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * (AuthUser)表服务实现类
 *
 * @author Wei
 * @since 2023-11-01 01:25:23
 */
@Service("authUserService")
public class AuthUserServiceImpl implements AuthUserService {
    @Resource
    private AuthUserDao authUserDao;

    /**
     * 通过ID查询单条数据
     */
    @Override
    public AuthUser queryById(Long id) {
        return this.authUserDao.queryById(id);
    }

    /**
     * 新增数据
     */
    @Override
    public Integer insert(AuthUser authUser) {
        return this.authUserDao.insert(authUser);
    }

    /**
     * 修改数据
     */
    @Override
    public Integer update(AuthUser authUser) {
        return this.authUserDao.update(authUser);
    }

    /**
     * 通过主键删除数据
     */
    @Override
    public boolean deleteById(Long id) {
        return this.authUserDao.deleteById(id) > 0;
    }

    /**
     * 根据条件查询
     */
    @Override
    public List<AuthUser> queryByCondition(AuthUser authUser) {
        return this.authUserDao.queryAllByLimit(authUser);
    }

    /**
     * 修改用户信息
     */
    @Override
    public Integer updateByUserName(AuthUser authUser) {
        return this.authUserDao.updateByUserName(authUser);
    }

    /**
     * 根据id批量查询
     */
    @Override
    public List<AuthUser> listUserInfoByIds(List<String> userNameList) {
        return authUserDao.listUserInfoByIds(userNameList);
    }

    /**
     * 查询用户专项练习次数
     */
    @Override
    public int queryPracticeCount(AuthUser authUser) {
        return authUserDao.queryPracticeCount(authUser);
    }

}
