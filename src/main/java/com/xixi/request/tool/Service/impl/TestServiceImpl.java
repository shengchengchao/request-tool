package com.xixi.request.tool.Service.impl;

import com.xixi.request.tool.Service.TestService;
import org.springframework.stereotype.Service;

/**
 * @author shengchengchao
 * @Description
 * @createTime 2021/3/15
 */
@Service
public class TestServiceImpl implements TestService {

    @Override
    public String getData() {
        return "userId";
    }
}
