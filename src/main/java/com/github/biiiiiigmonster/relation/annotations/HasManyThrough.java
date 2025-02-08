package com.github.biiiiiigmonster.relation.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Relation
public @interface HasManyThrough {
    String localKey() default "";
    String foreignKey() default "";
}
