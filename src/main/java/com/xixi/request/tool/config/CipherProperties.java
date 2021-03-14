package com.xixi.request.tool.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author shengchengchao
 * @Description
 * @createTime 2021/3/14
 */
@ConfigurationProperties(prefix = "request.tool.cipher")
@Component
public class CipherProperties {
    public static final String DEFAlUT_KEY = "requestTool";

    private String key = DEFAlUT_KEY;



    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
