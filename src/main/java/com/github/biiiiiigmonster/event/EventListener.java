package com.github.biiiiiigmonster.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 事件监听器注解，用于标记事件监听器方法
 * 参考Laravel的@EventListener注解
 * 
 * @author luyunfeng
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventListener {
    /**
     * 事件类型，如果不指定则从方法参数推断
     */
    Class<?> value() default Void.class;
    
    /**
     * 是否异步执行
     */
    boolean async() default false;
    
    /**
     * 优先级，数字越小优先级越高
     */
    int priority() default 0;
}