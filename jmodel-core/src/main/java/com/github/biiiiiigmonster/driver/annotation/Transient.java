package com.github.biiiiiigmonster.driver.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记字段为非持久化字段，不会被保存到数据库
 * 用于替代 MyBatis-Plus 的 @TableField(exist = false) 注解
 * 
 * @author jmodel-core
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Transient {
}