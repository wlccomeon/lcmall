package com.lcmall.common;

public class Const {
    public static final String CURRENT_USER = "currentUser";
    public static final String EMAIL = "email";
    public static final String USERNAME = "username";
    public static final String TOKEN_PREFIX = "token_";
    /**
     * 角色常量
     */
    public interface Role{
        /**普通用户*/
        int ROLE_CUSTOMER = 0;
        /**管理员*/
        int ROLE_ADMIN = 1;
    }

    /**
     * redis缓存常量
     */
    public interface RedisCache{
        //30分钟
        int SESSION_EXTIME = 60 * 30;
    }
}
