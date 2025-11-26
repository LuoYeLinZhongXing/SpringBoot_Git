package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.swing.text.html.parser.Entity;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;


/**
 * 自定义切面类，实现公共字段自动填充
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 切入点和切入点表达式
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPoonCut() {}

    /**
     * 前置通知，在方法执行前进行调用
     */
    @Before("autoFillPoonCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行公共字段填充");

        //获取到当前被拦截的方法上的数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//获取方法签名
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

        //获取当前被拦截的方法参数
        Object[] args = joinPoint.getArgs();
        if(args==null || args.length==0){
            return;
        }

        Object arg = args[0];

        //根据不同的操作类型，为不同的属性赋值
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        if(operationType== OperationType.INSERT){
            //为插入操作的字段赋值

            //通过反射赋值
            try {
                Method setCreateTime = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTime = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUser = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                setCreateTime.invoke(arg,now);
                setUpdateTime.invoke(arg,now);
                setCreateUser.invoke(arg,currentId);
                setUpdateUser.invoke(arg,currentId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }else if(operationType== OperationType.UPDATE){
            //为更新操作的字段赋值
            try {
                Method setUpdateTime = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                setUpdateTime.invoke(arg,now);
                setUpdateUser.invoke(arg,currentId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
}
