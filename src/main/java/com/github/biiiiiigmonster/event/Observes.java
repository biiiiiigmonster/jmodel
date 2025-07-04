package com.github.biiiiiigmonster.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 观察者注解，用于标记模型观察者对应的模型类型
 * 参考Laravel的Observer注册机制
 * 
 * @author luyunfeng
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Observes {
    /**
     * 要观察的模型类型
     */
    Class<?> value();
}