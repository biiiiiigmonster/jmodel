package io.github.biiiiiigmonster.relation.annotation.config;

import io.github.biiiiiigmonster.Model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MorphAlias {
    String value() default "";
    Class<? extends Model<?>>[] in() default {};
}
