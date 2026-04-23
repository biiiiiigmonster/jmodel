package io.github.biiiiiigmonster.driver.entity;

import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.driver.CriterionType;
import io.github.biiiiiigmonster.relation.annotation.BelongsTo;
import io.github.biiiiiigmonster.relation.annotation.HasMany;
import io.github.biiiiiigmonster.relation.annotation.MorphedByMany;
import io.github.biiiiiigmonster.relation.constraint.Constraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class Tag extends Model<Tag> {
    private Long id;
    private String name;
    private Long parentId;

    @BelongsTo(foreignKey = "parentId")
    private Tag parent;

    @HasMany(foreignKey = "parentId")
    private List<Tag> children;

    @MorphedByMany(using = Taggable.class)
    private List<Post> posts;

    @MorphedByMany(using = Taggable.class)
    private List<Video> videos;

    /**
     * 中间表（pivot）静态约束测试：通过 Taggable.id > 10 过滤中间表。
     * Tag 1 对应 Post 的 Taggable 为 {1, 21}，约束后只剩 {21} → Post 6。
     * <p>
     * 显式指定 {@code foreignPivotKey} 与 {@code pivotId}（即 Taggable 中指向 Tag 与
     * 指向多态实体的键），避免受 MorphedByMany 默认键推断行为影响。
     */
    @MorphedByMany(using = Taggable.class,
            foreignPivotKey = "tagId",
            pivotId = "taggableId",
            pivotConstraints = {
                    @Constraint(field = "id", type = CriterionType.GT, value = "10")
            })
    private List<Post> pivotFilteredPosts;
}
