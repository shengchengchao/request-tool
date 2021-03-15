package com.xixi.request.tool.advice;

import com.xixi.request.tool.annonation.Cipher;
import com.xixi.request.tool.base.BaseResult;
import com.xixi.request.tool.config.CipherProperties;
import com.xixi.request.tool.util.AESUtil;
import com.xixi.request.tool.util.StreamUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author shengchengchao
 * @Description
 * @createTime 2021/3/14
 */
@ControllerAdvice
@Slf4j
public class EncryptResponse implements ResponseBodyAdvice<BaseResult> {

    @Autowired
    private CipherProperties cipherProperties;
    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
        return methodParameter.getMethod().isAnnotationPresent(Cipher.class);
    }

    @Override
    public BaseResult beforeBodyWrite(BaseResult o, MethodParameter methodParameter, MediaType mediaType, Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        log.info("开始对返回值进行加密操作!");
        Cipher annotation = methodParameter.getMethod().getAnnotation(Cipher.class);
        boolean encrtpt = annotation.responsEncrypt();
        try {
            if(encrtpt){
                if(o.getMessage()!=null){
                    o.setMessage(AESUtil.encrypt(o.getMessage().getBytes(),cipherProperties.getKey().getBytes()));
                }
                if(o.getData()!=null){
                    byte[] bytes = StreamUtil.toByteArray(o.getData());
                    o.setMessage(AESUtil.encrypt(bytes,cipherProperties.getKey().getBytes()));
                }
            }
        } catch (Exception e) {
            log.error("加密出错",e);
        }
        return o;
    }
}
