package com.github.biiiiiigmonster.relation.annotation;

import com.github.biiiiiigmonster.relation.MorphPivot;
import com.github.biiiiiigmonster.relation.annotation.config.Relation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Relation
public @interface MorphToMany {
    Class<? extends MorphPivot<?>> using();
    String pivotType() default "";
    String pivotId() default "";
    String relatedPivotKey() default "";
    String foreignKey() default "";
    String localKey() default "";
    boolean withPivot() default false;
}
