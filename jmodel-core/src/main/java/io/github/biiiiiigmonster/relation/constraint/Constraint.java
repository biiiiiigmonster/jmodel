package io.github.biiiiiigmonster.relation.constraint;

import io.github.biiiiiigmonster.driver.CriterionType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 静态查询约束声明。
 * <p>
 * 用于关系注解（如 {@code @HasMany}）的 {@code constraints()} 属性，在加载关联时
 * 自动追加到目标关联模型的查询条件上。
 * <p>
 * 由于注解参数必须是编译期常量，{@link #value()} 统一使用 {@code String[]}，
 * 在应用到 {@link io.github.biiiiiigmonster.driver.QueryCondition} 时会按照
 * 关联实体对应字段的 Java 类型做类型转换。
 * <p>
 * 示例：
 * <pre>
 * &#64;HasMany(foreignKey = "userId", constraints = {
 *     &#64;Constraint(field = "status", value = "published"),
 *     &#64;Constraint(field = "viewCount", type = CriterionType.GT, value = "100")
 * })
 * private List&lt;Post&gt; publishedPosts;
 * </pre>
 *
 * @author biiiiiigmonster
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Constraint {
    /**
     * 关联模型上的字段名
     */
    String field();

    /**
     * 条件类型，默认 {@link CriterionType#EQ}
     */
    CriterionType type() default CriterionType.EQ;

    /**
     * 字段值；
     * <ul>
     *     <li>{@code EQ / GT / LT / LIKE}：取 {@code value[0]}</li>
     *     <li>{@code IN}：取全部元素</li>
     *     <li>{@code IS_NULL / IS_NOT_NULL}：无需提供</li>
     * </ul>
     */
    String[] value() default {};
}
