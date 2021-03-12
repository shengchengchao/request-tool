package com.xixi.request.tool.Interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author shengchengchao
 * @Description 拦截器 主要是对于参数进行一个解密
 * @createTime 2021/3/12
 */
public class HandlerIntercept implements HandlerInterceptor {

    /**
     * 前置通知
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public  boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

}
