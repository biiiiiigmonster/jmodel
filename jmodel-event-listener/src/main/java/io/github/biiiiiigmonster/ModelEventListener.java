package io.github.biiiiiigmonster;

import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@TransactionalEventListener
public @interface ModelEventListener {
    Class<? extends Model<?>>[] models() default {};

    @AliasFor(annotation = TransactionalEventListener.class, attribute = "phase")
    TransactionPhase phase() default TransactionPhase.AFTER_COMMIT;

    @AliasFor(annotation = TransactionalEventListener.class, attribute = "fallbackExecution")
    boolean fallbackExecution() default false;

    @AliasFor(annotation = TransactionalEventListener.class, attribute = "classes")
    Class<?>[] value() default {};

    @AliasFor(annotation = TransactionalEventListener.class, attribute = "classes")
    Class<?>[] classes() default {};

    @AliasFor(annotation = TransactionalEventListener.class, attribute = "condition")
    String condition() default "";

    @AliasFor(annotation = TransactionalEventListener.class, attribute = "id")
    String id() default "";
}
