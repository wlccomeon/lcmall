package com.lcmall.common.filter;

import com.lcmall.common.Const;
import com.lcmall.po.User;
import com.lcmall.util.CookieUtil;
import com.lcmall.util.JsonUtil;
import com.lcmall.util.redis.RedisShardedUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 设置"Session"的过期时间过滤器
 * 当用户登录之后，每操作一次请求，则将"session"的有效期延长
 * 这里的"session"其实是key为sessionId，value为user信息的缓存
 * @author wlc
 */
public class SessionExpireFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //将ServletRequest转换为HttpServletRequest
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        String token = CookieUtil.readCookieValue(request);
        //如果token不为空的话，符合条件，则获取user信息，user不为空，则将redis缓存中的session时间重置为指定时时长
        if(StringUtils.isNotBlank(token)){
            String userJsonStr = RedisShardedUtil.get(token);
            User user = JsonUtil.string2Obj(userJsonStr,User.class);
            if(user != null){
                //如果user不为空，则重置session的时间，即调用expire命令
                RedisShardedUtil.expire(token, Const.RedisCache.SESSION_EXTIME);
            }
        }
        filterChain.doFilter(servletRequest,servletResponse);
    }

    @Override
    public void destroy() {

    }
}
