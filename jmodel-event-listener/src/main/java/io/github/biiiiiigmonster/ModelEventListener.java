package io.github.biiiiiigmonster;

import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 模型事件监听器注解，在 {@link TransactionalEventListener} 基础上增加 {@link #models()} 属性，
 * 用于按模型类型过滤 {@link io.github.biiiiiigmonster.event.ModelEvent}。
 * <p>
 * {@code models} 为空时表示不过滤模型类型；与 {@link #condition()} 共存时，二者以逻辑与（{@code &&}）合并。
 *
 * @see io.github.biiiiiigmonster.event.ModelEvent
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@TransactionalEventListener
public @interface ModelEventListener {

    /**
     * 监听的模型类型；为空时不按模型类型过滤。
     */
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
}
