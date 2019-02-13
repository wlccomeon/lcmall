package com.lcmall.util;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * cookie操作工具类
 * @author wlc
 */
@Slf4j
public class CookieUtil {

    /**设置为一级域名，子域名就可以共享该一级域名下的cookie*/
    private static final String COOKIE_DOMAIN = ".lcmall.com";
    /**设置cookie的目录为根目录"/",子级目录可以共享*/
    private static final String COOKIE_PATH = "/";
    /**设置cookie的name，读写均使用它*/
    private static final String COOKIE_NAME = "login_token";

    /**
     * 读取cookie中的value
     * @param request
     * @return
     */
    public static String readCookieValue(HttpServletRequest request){
        Map<String,Cookie> cookieMap = getCookieMap(request);
        if (cookieMap.containsKey(COOKIE_NAME)){
            Cookie cookie = cookieMap.get(COOKIE_NAME);
            log.info("return cookieName:{},cookieValue:{}",cookie.getName(),cookie.getValue());
            return cookie.getValue();
        }
        return null;
    }

    /**
     * 写入cookie,下面解释一下domain与path
     *     //X:domain=".lcmall.com"
     *     //a:A.lcmall.com            cookie:domain=A.lcmall.com;path="/"
     *     //b:B.lcmall.com            cookie:domain=B.lcmall.com;path="/"
     *     //c:A.lcmall.com/test/cc    cookie:domain=A.lcmall.com;path="/test/cc"
     *     //d:A.lcmall.com/test/dd    cookie:domain=A.lcmall.com;path="/test/dd"
     *     //e:A.lcmall.com/test       cookie:domain=A.lcmall.com;path="/test"
     *
     *     //由于domain和path的设置以上的结果如下：
     *     //a,b,c,d,e都能拿到X这个domain下的cookie
     *     //a与b相互之间是拿不到之间的cookie的
     *     //c与d均能够共享a与e产生的cookie
     *     //a与b相互之间是拿不到之间的cookie的，c、d均拿不到b的
     * @param response
     * @param token
     * @return
     */
    public static void writeCookie(HttpServletResponse response, String token){
        Cookie cookie = new Cookie(COOKIE_NAME,token);
        cookie.setDomain(COOKIE_DOMAIN);
        //设置cookie的访问仅通过http方式，可一定程度防止脚本攻击
        cookie.setHttpOnly(true);
        //如果不设置该值，则cookie不会保存到硬盘中，只存在于内存中，只在当前页面有效。
        //单位为s，这里设置为一年，如果设置为-1，则代表永久
        cookie.setMaxAge(60*60*24*365);
        cookie.setPath(COOKIE_PATH);
        log.info("wirte cookie name：{}，value：{}",cookie.getName(),cookie.getValue());
        response.addCookie(cookie);
    }

    /**
     * 删除cookie
     * @param response
     * @return
     */
    public static void delCookie(HttpServletRequest request,HttpServletResponse response){
        Map<String,Cookie> cookieMap = getCookieMap(request);
        if (cookieMap.containsKey(COOKIE_NAME)){
            Cookie cookie = cookieMap.get(COOKIE_NAME);
            cookie.setDomain(COOKIE_DOMAIN);
            cookie.setPath(COOKIE_PATH);
            //设置成0，代表删除此cookie
            cookie.setMaxAge(0);
            log.info("del cookieName:{},cookieValue:{}",cookie.getName(),cookie.getValue());
            response.addCookie(cookie);
        }
    }

    /**
     * 将request中的cookie包装成一个map，实现代码复用
     * @param request
     * @return
     */
    private static Map<String,Cookie> getCookieMap(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        Map<String,Cookie> cookieMap = new HashMap<>();
        if (cookies != null){
            for (Cookie cookie: cookies) {
                cookieMap.put(cookie.getName(),cookie);
            }
        }
        return cookieMap;
    }
}
