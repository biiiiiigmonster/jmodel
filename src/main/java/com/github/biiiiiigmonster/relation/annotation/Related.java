package com.github.biiiiiigmonster.relation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * 此注解用来指定数据加载方法，约定情况下@RelatedRepository即可
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/10/10 16:46
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Related {
    String field();
}
