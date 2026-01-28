package io.github.biiiiiigmonster.relation.annotation;

import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.relation.annotation.config.Relation;

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
