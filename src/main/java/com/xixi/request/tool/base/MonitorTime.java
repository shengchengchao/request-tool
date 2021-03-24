package com.xixi.request.tool.base;

import lombok.Data;

import java.util.Date;

/**
 * @author shengchengchao
 * @Description
 * @createTime 2021/3/21
 */
@Data
public class MonitorTime {

    private String className;
    private String methodName;
    private Date logTime;
    private long comsumeTime;
}
