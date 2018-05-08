package com.joey.myspringmvc.annotation;

import java.lang.annotation.*;

/**
 * Created by xiaowu.zhou@tongdun.cn on 2018/5/8.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyController {

    String value() default "";
}
