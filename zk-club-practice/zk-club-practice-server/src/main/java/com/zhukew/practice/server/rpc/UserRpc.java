package com.zhukew.practice.server.rpc;

import com.zhukew.auth.api.UserFeignService;
import com.zhukew.auth.entity.AuthUserDTO;
import com.zhukew.auth.entity.Result;
import com.zhukew.practice.server.entity.dto.UserInfo;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class UserRpc {

    @Resource
    private UserFeignService userFeignService;

    public UserInfo getUserInfo(String userName) {
        AuthUserDTO authUserDTO = new AuthUserDTO();
        authUserDTO.setUserName(userName);
        Result<AuthUserDTO> result = userFeignService.getUserInfo(authUserDTO);
        UserInfo userInfo = new UserInfo();
        if (!result.getSuccess()) {
            return userInfo;
        }
        AuthUserDTO data = result.getData();
        userInfo.setUserName(data.getUserName());
        userInfo.setNickName(data.getNickName());
        userInfo.setAvatar(data.getAvatar());
        return userInfo;
    }

}
