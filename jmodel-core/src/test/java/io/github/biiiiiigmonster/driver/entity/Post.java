package io.github.biiiiiigmonster.driver.entity;

import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.driver.CriterionType;
import io.github.biiiiiigmonster.relation.annotation.BelongsTo;
import io.github.biiiiiigmonster.relation.annotation.HasMany;
import io.github.biiiiiigmonster.relation.annotation.HasOneDeep;
import io.github.biiiiiigmonster.relation.annotation.MorphMany;
import io.github.biiiiiigmonster.relation.annotation.MorphOne;
import io.github.biiiiiigmonster.relation.annotation.MorphToMany;
import io.github.biiiiiigmonster.relation.annotation.SiblingMany;
import io.github.biiiiiigmonster.relation.annotation.config.MorphAlias;
import io.github.biiiiiigmonster.relation.annotation.config.Via;
import io.github.biiiiiigmonster.relation.constraint.Constraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@MorphAlias
public class Post extends Model<Post> {
    private Long id;
    private Long userId;
    private String title;

    @BelongsTo(foreignKey = "userId")
    private User user;

    /**
     * 深层 HasOne：帖子作者所在组织所属的地区。
     * Post → User → Membership → Organization → Region
     */
    @HasOneDeep({
            @Via(via = User.class, viaForeignKey = "id", localKey = "userId"),
            @Via(via = Membership.class),
            @Via(via = Organization.class, viaForeignKey = "id", localKey = "organizationId"),
            @Via(via = Region.class, viaForeignKey = "id", localKey = "regionId")
    })
    private Region authorRegion;

    @HasMany(foreignKey = "postId")
    private List<Likes> likes;

    @MorphOne
    private Image image;

    @MorphMany
    private List<Comment> comments;

    @MorphToMany(using = Taggable.class)
    private List<Tag> tags;

    /**
     * 中间表（pivot）静态约束测试：通过 Taggable.tagId > 1 过滤中间表。
     * Post 1 默认 tags 为 [Java(1), Spring(2)]；约束后只剩 [Spring(2)]。
     */
    @MorphToMany(using = Taggable.class, pivotConstraints = {
            @Constraint(field = "tagId", type = CriterionType.GT, value = "1")
    })
    private List<Tag> pivotFilteredTags;

    @SiblingMany(from = "user")
    private List<Post> siblingsUserPosts;
}
