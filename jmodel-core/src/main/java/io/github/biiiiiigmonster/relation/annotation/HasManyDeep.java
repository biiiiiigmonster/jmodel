package io.github.biiiiiigmonster.relation.annotation;

import io.github.biiiiiigmonster.relation.annotation.config.Relation;
import io.github.biiiiiigmonster.relation.annotation.config.Via;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Relation(resultList = true)
public @interface HasManyDeep {
    Via[] value();
}
