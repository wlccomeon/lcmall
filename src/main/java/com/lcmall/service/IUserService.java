package com.lcmall.service;

import com.lcmall.common.ServerResponse;
import com.lcmall.po.User;
import org.springframework.stereotype.Service;


public interface IUserService {

    /**
     * 登录方法
     * @param username 用户名
     * @param password 密码
     * @return
     */
    ServerResponse<User> login(String username,String password);
}
