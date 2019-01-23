package com.lcmall.service.impl;

import com.lcmall.common.ServerResponse;
import com.lcmall.dao.UserMapper;
import com.lcmall.po.User;
import com.lcmall.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceIpml implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int result = userMapper.checkUsername(username);
        if (result==0){
            return ServerResponse.createByErrorMessage("用户名不存在.");
        }
        User user = userMapper.selectLogin(username,password);
        if (user==null){
            return ServerResponse.createByErrorMessage("密码不正确");
        }
        //登录成功则将密码置空，防止页面捕捉
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

}
