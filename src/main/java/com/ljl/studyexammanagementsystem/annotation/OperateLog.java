package com.ljl.studyexammanagementsystem.annotation;

import java.lang.annotation.*;

/**
 * 操作日志AOP注解
 * 标注在Service方法上，方法执行后自动记录操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperateLog {

    /**
     * 操作模块/描述
     */
    String module() default "";

    /**
     * 操作类型：LOGIN-登录, LOGOUT-退出, RESET_PWD-重置密码, UNLOCK-解锁, OTHER-其他
     */
    String type() default "OTHER";
}
