package com.github.biiiiiigmonster.relation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Relation
public @interface MorphOne {
    String name();
    String type() default "";
    String id() default "";
    String localKey() default "";
}
