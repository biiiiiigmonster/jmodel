package com.github.biiiiiigmonster.relation.annotations;

import com.github.biiiiiigmonster.Model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Relation
public @interface HasManyThrough {
    Class<? extends Model<?>> through();
    String foreignKey() default "";
    String throughForeignKey() default "";
    String localKey() default "";
    String throughLocalKey() default "";
}
