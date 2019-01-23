package com.lcmall.test;

import com.lcmall.common.ServerResponse;
import com.lcmall.po.User;
import com.lcmall.service.IUserService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 测试user相关方法
 */
public class UserTest extends BaseTest {

    @Autowired
    private IUserService iUserService;

    @Test
    public void testLogin(){
        String username = "admin";
        String password = "427338237BD929443EC5D48E24FD2B1A";

        ServerResponse<User> response = iUserService.login(username,password);
        Assert.assertTrue(response.getData()!=null);
        System.out.println(((User)response.getData()).toString());
    }




}
