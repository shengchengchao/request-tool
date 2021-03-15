package com.xixi.request.tool.Interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xixi.request.tool.Service.TestService;
import com.xixi.request.tool.annonation.AuthVerification;
import com.xixi.request.tool.dto.Testdto;
import com.xixi.request.tool.wrapper.CustomHttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @author shengchengchao
 * @Description
 * @createTime 2021/3/15
 */
@Slf4j
@Component
public class RequestInterceptor implements HandlerInterceptor {

    @Autowired
    private TestService testService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;

        test(request,handlerMethod);

        return checkAuth(handlerMethod,request);
    }

    /**
     * 进行权限的校验 这里只校验大方面的问题，校验是否具有权限，后续细分的权限要独立校验
     * 权限 要获取用户id的话，推荐使用传入token，再过滤器中进行解析后，将结果放入到header中，后续从header中拿
     * @param handlerMethod
     * @return
     */
    private boolean checkAuth(HandlerMethod handlerMethod,HttpServletRequest httpServletRequest) {
        Method method = handlerMethod.getMethod();
        AuthVerification annotation = method.getAnnotation(AuthVerification.class);
        return annotation.getAuth();
    }

    public void test(HttpServletRequest request, HandlerMethod handlerMethod){
        try {
            String userId = testService.getData();

            MethodParameter[] methodParameters = handlerMethod.getMethodParameters();
            if (ArrayUtils.isEmpty(methodParameters)) {
                return;
            }
            for (MethodParameter methodParameter : methodParameters) {
                // 注意 这里 要进行参数的设置的话，要先经过过滤器进行数据的包装，
                // 才能生效，不经过过滤器的话，不会生效
                if (request instanceof CustomHttpServletRequestWrapper) {
                    CustomHttpServletRequestWrapper requestWrapper = (CustomHttpServletRequestWrapper) request;
                    String body = requestWrapper.getBody();
                    JSONObject param = null;
                    try {
                        param = JSONObject.parseObject(body);
                        if (StringUtils.isNotBlank(userId) && param != null) {
                            param.put("userId", userId);
                        }
                        requestWrapper.setBody(JSON.toJSONString(param));

                    } catch (Exception e) {

                    }

                } else {

                }
            }
        } catch (Exception e) {

            log.warn("fill userInfo to request body Error ", e);
        }
    }
}
