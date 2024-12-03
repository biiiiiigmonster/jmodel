package com.biiiiiigmonster.octopus.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 *
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/10/26 15:35
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Computed {
    String field() default "";
}
