package com.lcmall.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * SpringMVC的全局异常处理类
 * @author wlc
 */
@Slf4j
@Component
public class ExceptionResolver implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {
        //将请求地址及所产生的错误打印到后台
        log.error("{} Exception",httpServletRequest.getRequestURI(),e);
        //这里不需要返回给前端一个错误页面，返回Json数据即可。通过ModelAndView的有参(参数为View)构造器，注入一个View的子类MappingJacksonJsonView
        //MappingJacksonJsonView对应的pom文件中Jackson2.0以下版本，如果使用的2.0以上版本，则使用MappingJackson2JsonView
        ModelAndView mv = new ModelAndView(new MappingJacksonJsonView());
        //可以不加参数，不过最好按照Controller中返回的那种参数格式即ServerResponse的格式来，方便前端统一处理。
        mv.addObject("code",ResponseCode.ERROR.getCode());
        mv.addObject("msg","发生异常，请联系管理员。");
        //返回给前端一个错误标题
        mv.addObject("data",e.getMessage());
        return mv;
    }
}
