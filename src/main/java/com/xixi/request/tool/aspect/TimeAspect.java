package com.xixi.request.tool.aspect;

import com.xixi.request.tool.base.MonitorTime;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author shengchengchao
 * @Description
 * @createTime 2021/3/21
 */
@Aspect
@Slf4j
public class TimeAspect {

    @Around("bean(*Controller)")
    public Object CalculatTime(ProceedingJoinPoint thisJoinPoint) throws Throwable{
        //获取目标类名称
        String clazzName = thisJoinPoint.getTarget().getClass().getName();
        //获取目标类方法名称
        String methodName = thisJoinPoint.getSignature().getName();
        MonitorTime monitorTime = new MonitorTime();
        monitorTime.setClassName(clazzName);
        monitorTime.setMethodName(methodName);
        long startTime = System.currentTimeMillis();
        Object result = thisJoinPoint.proceed();
        long endTime = System.currentTimeMillis();
        monitorTime.setComsumeTime(endTime-startTime);
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");
        long date_temp = monitorTime.getComsumeTime();
        String date_string = sdf.format(new Date(date_temp * 1000L));

        log.info("方法:{} 进行请求，耗费的时间为:{}",methodName,date_string);
        return result;
    }

}
