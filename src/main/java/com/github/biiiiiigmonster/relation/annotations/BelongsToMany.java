package com.github.biiiiiigmonster.relation.annotations;

import com.github.biiiiiigmonster.relation.Pivot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Relation
public @interface BelongsToMany {
    Class<? extends Pivot<?>> using();
    String foreignPivotKey() default "";
    String relatedPivotKey() default "";
    String localKey() default "";
    String foreignKey() default "";
}
