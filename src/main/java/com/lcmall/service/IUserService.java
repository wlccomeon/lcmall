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

    /**
     * 注册
     * @param user
     * @return
     */
    ServerResponse<String> addUser(User user);

    /**
     * 校验用户信息
     * @param str   user字段中的值
     * @param type  user字段中的key
     * @return
     */
    ServerResponse<String> selectAndCheckValid(String str,String type);

    /**
     * 根据用户名查询问题
     * @param username
     * @return
     */
    ServerResponse selectQuestion(String username);

    /**
     * 校验回答问题的正确性
     * @param username 用户名
     * @param question 问题
     * @param answer 问题答案
     * @return
     */
    ServerResponse<String> selectAndCheckAnswer(String username,String question,String answer);

    /**
     * 忘记并重置密码
     * @param username    用户名
     * @param passwordNew 新密码
     * @param forgetToken 回答问题正确之后返回给页面的token
     * @return
     */
    ServerResponse<String> updateForgetPassword(String username,String passwordNew,String forgetToken);

    /**
     * 重置密码
     * @param passwordOld 旧密码
     * @param passwordNew 新密码
     * @param user 用户信息
     * @return
     */
    ServerResponse<String> updatePassword(String passwordOld,String passwordNew,User user);

    /**
     * 更新用户信息
     * @param user 用户信息
     * @return
     */
    ServerResponse<User> updateInformation(User user);

    /**
     * 获取用户信息
     * @param userId 用户id
     * @return
     */
    ServerResponse<User> getInformation(Integer userId);

    /**
     * 校验管理员角色
     * @param user 用户信息
     * @return
     */
    ServerResponse checkAdminRole(User user);
}
