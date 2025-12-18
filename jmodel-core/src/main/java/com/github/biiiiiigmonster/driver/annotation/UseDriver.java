package com.github.biiiiiigmonster.driver.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于指定模型使用的数据驱动
 * 
 * @author jmodel-core
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UseDriver {
    /**
     * 驱动名称
     * 
     * @return 驱动标识符
     */
    String value();
}