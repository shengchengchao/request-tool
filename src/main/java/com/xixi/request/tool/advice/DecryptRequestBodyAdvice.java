package com.xixi.request.tool.advice;

import com.google.common.io.ByteStreams;
import com.xixi.request.tool.annonation.Cipher;
import com.xixi.request.tool.config.CipherProperties;
import com.xixi.request.tool.util.AESUtil;
import com.xixi.request.tool.util.StreamUtil;
import io.swagger.models.auth.In;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

/**
 * @author shengchengchao
 * @Description
 * @createTime 2021/3/14
 */
@Slf4j
@ControllerAdvice
public class DecryptRequestBodyAdvice extends RequestBodyAdviceAdapter {

    @Autowired
    private CipherProperties cipherProperties;
    /**
     * 判断这个接口是否支持
     * @param methodParameter
     * @param type
     * @param aClass
     * @return
     */
    @Override
    public boolean supports(MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        return methodParameter.getMethod().isAnnotationPresent(Cipher.class);
    }

    @SneakyThrows
    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) throws IOException {

        Cipher annotation = methodParameter.getMethod().getAnnotation(Cipher.class);
        log.info("开始解密");
        boolean decrypt = annotation.requestDecrypt();
        if(decrypt){
            try {
                byte[] body = new byte[httpInputMessage.getBody().available()];
                httpInputMessage.getBody().read(body);
                byte[] bytes = AESUtil.decrypt(body, cipherProperties.getKey().getBytes());
                final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                return new HttpInputMessage() {
                    @Override
                    public InputStream getBody() throws IOException {
                        return bais;
                    }

                    @Override
                    public HttpHeaders getHeaders() {
                        return httpInputMessage.getHeaders();
                    }
                };
            } catch (Exception e) {
                log.error("出错了",e);
                e.printStackTrace();
            }
        }

        return super.beforeBodyRead(httpInputMessage, methodParameter, type, aClass);
    }





}
