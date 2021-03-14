package com.xixi.request.tool.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xixi.request.tool.config.CipherProperties;
import com.xixi.request.tool.util.AESUtil;
import com.xixi.request.tool.wrapper.CustomHttpServletRequestWrapper;
import com.xixi.request.tool.wrapper.ResponseWrapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author shengchengchao
 * @Description 过滤器 主要是对结果进行一个加密
 * @createTime 2021/3/12
 */
@Slf4j
@WebFilter("/t")
public class MyFilter implements Filter {

    @Autowired
    private CipherProperties cipherProperties;
    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        log.info("进入过滤器");
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        CustomHttpServletRequestWrapper requestWrapper = new CustomHttpServletRequestWrapper(httpRequest) ;
        //从 Requestparamter中获得参数
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        //得到 @PathVariable 的参数
        //Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        // String Id = (String)pathVariables.get("Id");
        String body = requestWrapper.getBody();
        byte[] decrypt = AESUtil.decrypt(body.getBytes(), cipherProperties.getKey().getBytes());
        requestWrapper.setBody(new String(decrypt));
        ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse) servletResponse);
        filterChain.doFilter(requestWrapper, responseWrapper);
        byte[] bytes = responseWrapper.getBytes();

        String encrypt = AESUtil.encrypt(bytes, cipherProperties.getKey().getBytes());
        log.info("原本的内容:{},加密后的内容:{}",new String(bytes),encrypt);
        servletResponse.setContentType("application/json;charset=utf-8");
        servletResponse.setContentLength(JSON.toJSONBytes(encrypt).length);
        servletResponse.getOutputStream().write(JSON.toJSONBytes(encrypt));

    }
}
