package com.xixi.request.tool.controller;

import com.xixi.request.tool.annonation.Cipher;
import com.xixi.request.tool.base.BaseResult;
import com.xixi.request.tool.dto.Testdto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

/**
 * @author shengchengchao
 * @Description
 * @createTime 2021/3/14
 */
@Api(tags = "测试")
@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {



    @ApiOperation(value = "测试加解密")
    @PostMapping("/encrypt")
    @ResponseBody
    @Cipher(requestDecrypt = true,responsEncrypt = true)
    public BaseResult<String> testData(@RequestBody Testdto testdto) {
        log.info("password" + testdto.getPassword());
        return new BaseResult<>(200,"成功",testdto.getPassword());
    }
}
