package com.xixi.request.tool.annonation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author shengchengchao
 * @Description 加密 解密的注解
 * @createTime 2021/3/12
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cipher {
    /**
     * 入参解密
     * @return
     */
    boolean requestDecrypt() default true;

    /**
     * 出参加密
     * @return
     */
    boolean responsEncrypt() default true;
}
