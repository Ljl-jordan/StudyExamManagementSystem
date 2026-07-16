package com.ljl.studyexammanagementsystem.annotation;

import java.lang.annotation.*;

/**
 * 登录日志AOP注解
 * 标注在方法上，方法执行后自动记录登录日志（成功/失败）
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LoginLog {

    /**
     * 操作描述
     */
    String value() default "登录操作";
}
