package io.github.biiiiiigmonster.relation.scope;

import io.github.biiiiiigmonster.driver.QueryCondition;
import io.github.biiiiiigmonster.driver.entity.Post;
import io.github.biiiiiigmonster.relation.constraint.RelationConstraint;

/**
 * 复用型约束：筛选标题中包含 "Spring" 的 Post
 */
public class SpringInTitleScope implements RelationConstraint<Post> {

    @Override
    public void apply(QueryCondition<Post> condition) {
        condition.like("title", "Spring");
    }
}
