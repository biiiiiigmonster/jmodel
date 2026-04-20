package io.github.biiiiiigmonster.relation.constraint;

import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.driver.QueryCondition;

import java.util.function.Consumer;

/**
 * 关联查询约束契约。
 * <p>
 * 使用者可以实现该接口以封装可复用的查询约束（类似 Laravel 的 Scope）：
 * <pre>
 * public class PublishedPostScope implements RelationConstraint&lt;Post&gt; {
 *     &#64;Override
 *     public void apply(QueryCondition&lt;Post&gt; cond) {
 *         cond.eq("status", "published");
 *     }
 * }
 *
 * // 使用
 * user.get(User::getPosts, new PublishedPostScope());
 * </pre>
 * <p>
 * 该接口既可以作为运行时动态约束直接传入 {@code get / load}，也可以作为静态约束
 * 通过关系注解的 {@code constraint()} 属性声明（由框架反射实例化）。
 *
 * @param <R> 关联模型类型
 * @author luyunfeng
 */
@FunctionalInterface
public interface RelationConstraint<R extends Model<?>> {

    /**
     * 对关联查询条件进行追加
     *
     * @param condition 关联模型查询条件
     */
    void apply(QueryCondition<R> condition);

    /**
     * 将 {@link Consumer} 适配为 {@link RelationConstraint}
     */
    static <R extends Model<?>> RelationConstraint<R> of(Consumer<QueryCondition<R>> consumer) {
        return consumer::accept;
    }

    /**
     * 默认空约束，作为注解默认值占位，确保反射实例化时不产生副作用
     */
    final class Noop implements RelationConstraint<Model<?>> {
        @Override
        public void apply(QueryCondition<Model<?>> condition) {
            // noop
        }
    }
}
