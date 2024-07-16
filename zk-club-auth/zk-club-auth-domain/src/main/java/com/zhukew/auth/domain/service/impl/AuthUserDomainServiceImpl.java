package com.zhukew.auth.domain.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhukew.auth.common.enums.AuthUserStatusEnum;
import com.zhukew.auth.common.enums.IsDeletedFlagEnum;
import com.zhukew.auth.domain.constants.AuthConstant;
import com.zhukew.auth.domain.convert.AuthUserBOConverter;
import com.zhukew.auth.domain.entity.AuthUserBO;
import com.zhukew.auth.domain.redis.RedisUtil;
import com.zhukew.auth.domain.service.AuthUserDomainService;
import com.zhukew.auth.infra.basic.entity.AuthPermission;
import com.zhukew.auth.infra.basic.entity.AuthRole;
import com.zhukew.auth.infra.basic.entity.AuthRolePermission;
import com.zhukew.auth.infra.basic.entity.AuthUser;
import com.zhukew.auth.infra.basic.entity.AuthUserRole;
import com.zhukew.auth.infra.basic.service.AuthPermissionService;
import com.zhukew.auth.infra.basic.service.AuthRolePermissionService;
import com.zhukew.auth.infra.basic.service.AuthRoleService;
import com.zhukew.auth.infra.basic.service.AuthUserRoleService;
import com.zhukew.auth.infra.basic.service.AuthUserService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户认证服务实现（domain层）
 *
 * @author: Wei
 */
@Service
@Slf4j
public class AuthUserDomainServiceImpl implements AuthUserDomainService {

    @Resource
    private AuthUserService authUserService;

    @Resource
    private AuthUserRoleService authUserRoleService;

    @Resource
    private AuthPermissionService authPermissionService;

    @Resource
    private AuthRolePermissionService authRolePermissionService;

    @Resource
    private AuthRoleService authRoleService;

    private String salt = "pig";

    @Resource
    private RedisUtil redisUtil;

    private String authPermissionPrefix = "auth.permission";

    private String authRolePrefix = "auth.role";

    private static final String LOGIN_PREFIX = "loginCode";

    /**
     * 用户注册
     */
    @Override
    @SneakyThrows
    @Transactional(rollbackFor = Exception.class)
    public Boolean register(AuthUserBO authUserBO) {
        //校验用户是否存在，存在则直接返回
        AuthUser existAuthUser = new AuthUser();
        existAuthUser.setUserName(authUserBO.getUserName());
        List<AuthUser> existUser = authUserService.queryByCondition(existAuthUser);
        if (existUser.size() > 0) {
            return true;
        }
        AuthUser authUser = AuthUserBOConverter.INSTANCE.convertBOToEntity(authUserBO);
        // 给用户赋默认值
        if (StringUtils.isNotBlank(authUser.getPassword())) {
            authUser.setPassword(SaSecureUtil.md5BySalt(authUser.getPassword(), salt));
        }
        if (StringUtils.isBlank(authUser.getAvatar())) {
            authUser.setAvatar("http://117.72.10.84:9000/user/icon/微信图片_20231203153718(1).png");
        }
        if (StringUtils.isBlank(authUser.getNickName())) {
            authUser.setNickName("猪客粉丝");
        }
        authUser.setStatus(AuthUserStatusEnum.OPEN.getCode());
        authUser.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        Integer count = authUserService.insert(authUser);

        //建立一个初步的角色的关联
        AuthRole authRole = new AuthRole();
        // 给新注册的用户安排普通用户的角色
        authRole.setRoleKey(AuthConstant.NORMAL_USER);
        AuthRole roleResult = authRoleService.queryByCondition(authRole);
        Long roleId = roleResult.getId();
        Long userId = authUser.getId();
        AuthUserRole authUserRole = new AuthUserRole();
        authUserRole.setUserId(userId);
        authUserRole.setRoleId(roleId);
        authUserRole.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        authUserRoleService.insert(authUserRole);

        // 将新用户的角色和权限存储到 redis 中
        String roleKey = redisUtil.buildKey(authRolePrefix, authUser.getUserName());
        List<AuthRole> roleList = new ArrayList<>();
        roleList.add(authRole);
        redisUtil.set(roleKey, new Gson().toJson(roleList));

        // 根据角色权限关联表查询 permissionId
        AuthRolePermission authRolePermission = new AuthRolePermission();
        authRolePermission.setRoleId(roleId);
        List<AuthRolePermission> rolePermissionList = authRolePermissionService.
                queryByCondition(authRolePermission);
        List<Long> permissionIdList = rolePermissionList.stream()
                .map(AuthRolePermission::getPermissionId).collect(Collectors.toList());

        // 根据 roleId 查权限并存入 redis 中
        List<AuthPermission> permissionList = authPermissionService.queryByRoleList(permissionIdList);
        String permissionKey = redisUtil.buildKey(authPermissionPrefix, authUser.getUserName());
        redisUtil.set(permissionKey, new Gson().toJson(permissionList));

        return count > 0;
    }

    /**
     * 更新用户信息
     */
    @Override
    public Boolean update(AuthUserBO authUserBO) {
        AuthUser authUser = AuthUserBOConverter.INSTANCE.convertBOToEntity(authUserBO);
        Integer count = authUserService.updateByUserName(authUser);
        return count > 0;
    }

    /**
     * 删除用户信息
     */
    @Override
    public Boolean delete(AuthUserBO authUserBO) {
        AuthUser authUser = new AuthUser();
        authUser.setId(authUserBO.getId());
        authUser.setIsDeleted(IsDeletedFlagEnum.DELETED.getCode());
        Integer count = authUserService.update(authUser);
        //有任何的更新，都要与缓存进行同步的修改
        return count > 0;
    }

    /**
     * 用户登录
     */
    @Override
    public SaTokenInfo doLogin(String validCode) {
        // redis 中存储了验证码
        String loginKey = redisUtil.buildKey(LOGIN_PREFIX, validCode);
        String openId = redisUtil.get(loginKey);
        if (StringUtils.isBlank(openId)) {
            return null;
        }
        AuthUserBO authUserBO = new AuthUserBO();
        authUserBO.setUserName(openId);
        // 未注册的用户先帮其注册
        this.register(authUserBO);
        // stpUtil将openId存入token中
        StpUtil.login(openId);
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        return tokenInfo;
    }

    /**
     * 获取用户信息
     */
    @Override
    public AuthUserBO getUserInfo(AuthUserBO authUserBO) {
        AuthUser authUser = new AuthUser();
        authUser.setUserName(authUserBO.getUserName());
        List<AuthUser> userList = authUserService.queryByCondition(authUser);
        if (CollectionUtils.isEmpty(userList)) {
            return new AuthUserBO();
        }
        AuthUser user = userList.get(0);
        return AuthUserBOConverter.INSTANCE.convertEntityToBO(user);
    }

    /**
     * 批量获取用户信息
     */
    @Override
    public List<AuthUserBO> listUserInfoByIds(List<String> userNameList) {
        List<AuthUser> userList = authUserService.listUserInfoByIds(userNameList);
        if (CollectionUtils.isEmpty(userList)) {
            return Collections.emptyList();
        }
        return AuthUserBOConverter.INSTANCE.convertEntityToBO(userList);
    }

    /**
     * 用户申请获得上传题目权限
     */
    @Override
    public Boolean subjectAddApplication(AuthUserBO authUserBO) {
        AuthUser authUser = new AuthUser();
        authUser.setUserName(authUserBO.getUserName());
        // 查询用户id，只会有一个结果
        List<AuthUser> authUsers = authUserService.queryByCondition(authUser);
        for (AuthUser user : authUsers) {
            authUser = user;
        }
        long interval = new Date().getTime() - authUser.getCreatedTime().getTime();
        int practiceCount = authUserService.queryPracticeCount(authUser);
        // 只有注册满 30 天并且专项练习数量超过 20 次才可获取该权限
        if(interval < (long)24*60*60*1000 || practiceCount < 20) {
            return false;
        }
        AuthRole authRole = new AuthRole();
        authRole.setRoleName("SubjectAdder");
        // 查询题目上传角色的id
        AuthRole role= authRoleService.queryByCondition(authRole);
        AuthUserRole authUserRole = new AuthUserRole();
        authUserRole.setRoleId((role.getId()));
        // 将用户的新角色插入到数据库中
        authUserRoleService.insert(authUserRole);

        // 将用户的新角色和权限存储到 redis 中
        String roleKey = redisUtil.buildKey(authRolePrefix, authUser.getUserName());
        // 先查出原有角色再添加新角色
        List<AuthRole> roleList = new Gson().fromJson(redisUtil.get(roleKey), new TypeToken<List<AuthRole>>() {}.getType());
        roleList.add(authRole);
        redisUtil.set(roleKey, new Gson().toJson(roleList));

        // 查询 subjectAdder 拥有的权限
        AuthRolePermission authRolePermission = new AuthRolePermission();
        authRolePermission.setRoleId(role.getId());
        List<AuthRolePermission> rolePermissionList = authRolePermissionService.
                queryByCondition(authRolePermission);
        List<Long> permissionIdList = rolePermissionList.stream()
                .map(AuthRolePermission::getPermissionId).collect(Collectors.toList());
        List<AuthPermission> newPermissionList = authPermissionService.queryByRoleList(permissionIdList);

        // 从redis中查出原有权限并添加新权限
        String permissionKey = redisUtil.buildKey(authPermissionPrefix, authUser.getUserName());
        List<AuthPermission> permissionList = new Gson().fromJson(redisUtil.get(permissionKey), new TypeToken<List<AuthPermission>>() {}.getType());
        permissionList.addAll(newPermissionList);
        redisUtil.set(permissionKey, new Gson().toJson(permissionList));

        return null;
    }

}
