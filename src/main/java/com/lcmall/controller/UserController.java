package com.lcmall.controller;

import com.lcmall.common.Const;
import com.lcmall.common.ResponseCode;
import com.lcmall.common.ServerResponse;
import com.lcmall.po.User;
import com.lcmall.service.IUserService;
import com.lcmall.util.CookieUtil;
import com.lcmall.util.JsonUtil;
import com.lcmall.util.redis.RedisShardedUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@Controller
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private IUserService iUserService;

    /**
     * 登录
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session, HttpServletResponse response){
        ServerResponse<User> serverResponse = iUserService.login(username,password);
        if (serverResponse.isSuccess()){
            String sessionId = session.getId();
            CookieUtil.writeCookie(response,sessionId);
            RedisShardedUtil.setEx(sessionId, JsonUtil.obj2String(serverResponse.getData()), Const.RedisCache.SESSION_EXTIME);
        }
        return serverResponse;
    }

    /**
     * 退出登录
     *
     * @return
     */
    @RequestMapping(value = "logout.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpServletRequest request,HttpServletResponse response){
        String token = CookieUtil.readCookieValue(request);
        //删除cookie数组中的自定义cookie
        CookieUtil.delCookie(request,response);
        //根据sessionid删除缓存中的用户数据
        RedisShardedUtil.del(token);
        return ServerResponse.createBySuccess();
    }

    /**
     * 注册
     * @param user
     * @return
     */
    @RequestMapping(value = "register.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user){
        return iUserService.addUser(user);
    }


    @RequestMapping(value = "check_valid.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str,String type){
        return iUserService.selectAndCheckValid(str,type);
    }

    /**
     * 获取Session中的用户信息
     * @return
     */
    @RequestMapping(value = "get_user_info.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpServletRequest request){
        String token = CookieUtil.readCookieValue(request);
        if (StringUtils.isNotBlank(token)){
            String userJsonStr = RedisShardedUtil.get(token);
            User user = JsonUtil.string2Obj(userJsonStr,User.class);
            if(user != null){
                return ServerResponse.createBySuccess(user);
            }
        }
        return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
    }

    /**
     * 忘记密码，获取问题
     * @param username
     * @return
     */
    @RequestMapping(value = "forget_get_question.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username){
        return iUserService.selectQuestion(username);
    }

    /**
     * 忘记密码，校验输入的答案是否正确
     * @param username
     * @param question
     * @param answer
     * @return
     */
    @RequestMapping(value = "forget_check_answer.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username,String question,String answer){
        return iUserService.selectAndCheckAnswer(username,question,answer);
    }

    /**
     * 校验答案正确之后，修改用户的密码
     * @param username    用户名
     * @param passwordNew 新密码
     * @param forgetToken 校验答案正确之后为页面返回的token信息
     * @return
     */
    @RequestMapping(value = "forget_reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetRestPassword(String username,String passwordNew,String forgetToken){
        return iUserService.updateForgetPassword(username,passwordNew,forgetToken);
    }


    /**
     * 登录状态下的重置密码
     * @param session
     * @param passwordOld
     * @param passwordNew
     * @return
     */
    @RequestMapping(value = "reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpSession session,String passwordOld,String passwordNew){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        return iUserService.updatePassword(passwordOld,passwordNew,user);
    }

    /**
     * 修改用户信息
     * @param session
     * @param user
     * @return
     */
    @RequestMapping(value = "update_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> update_information(HttpSession session,User user){
        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        ServerResponse<User> response = iUserService.updateInformation(user);
        if(response.isSuccess()){
            response.getData().setUsername(currentUser.getUsername());
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }

    /**
     * 获取数据库中的用户信息
     * @param session
     * @return
     */
    @RequestMapping(value = "get_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> get_information(HttpSession session){
        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录,需要强制登录status=10");
        }
        return iUserService.getInformation(currentUser.getId());
    }

}
