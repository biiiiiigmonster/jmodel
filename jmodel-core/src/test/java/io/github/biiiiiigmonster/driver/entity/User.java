package io.github.biiiiiigmonster.driver.entity;

import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.driver.CriterionType;
import io.github.biiiiiigmonster.relation.annotation.BelongsToMany;
import io.github.biiiiiigmonster.relation.annotation.HasMany;
import io.github.biiiiiigmonster.relation.annotation.HasManyThrough;
import io.github.biiiiiigmonster.relation.annotation.HasOne;
import io.github.biiiiiigmonster.relation.annotation.HasOneThrough;
import io.github.biiiiiigmonster.relation.annotation.MorphOne;
import io.github.biiiiiigmonster.relation.annotation.config.MorphAlias;
import io.github.biiiiiigmonster.relation.constraint.Constraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@MorphAlias
public class User extends Model<User> {
    private Long id;
    private String name;
    private String email;

    @HasOne
    private Phone phone;

    @HasOne
    private Profile profile;

    @HasOneThrough(through = Profile.class)
    private Address profileAddress;

    @HasMany(foreignKey = "userId")
    private List<Post> posts;

    @HasMany(foreignKey = "userId", chaperone = true)
    private List<Post> postChaperones;

    @HasManyThrough(through = Post.class)
    private List<Likes> commentLikes;

    @BelongsToMany(using = UserRole.class)
    private List<Role> roles;

    @BelongsToMany(using = UserRole.class, withPivot = true)
    private List<Role> roleWithPivots;

    @MorphOne
    private Image image;

    /**
     * 静态注解约束：标题中包含 "Spring" 的 Post
     */
    @HasMany(foreignKey = "userId", constraints = {
            @Constraint(field = "title", type = CriterionType.LIKE, value = "Spring")
    })
    private List<Post> springPosts;

    /**
     * 静态注解约束：id > 5 的 Post
     */
    @HasMany(foreignKey = "userId", constraints = {
            @Constraint(field = "id", type = CriterionType.GT, value = "5")
    })
    private List<Post> highIdPosts;

    /**
     * 静态注解约束：指定标题集合（IN）
     */
    @HasMany(foreignKey = "userId", constraints = {
            @Constraint(field = "title", type = CriterionType.IN, value = {
                    "Docker Best Practices",
                    "Kubernetes in Practice"
            })
    })
    private List<Post> dockerOrK8sPosts;

    /**
     * 静态 + 运行时叠加场景
     */
    @HasMany(foreignKey = "userId", constraints = {
            @Constraint(field = "id", type = CriterionType.GT, value = "0")
    })
    private List<Post> constrainedPosts;

    /**
     * 终表约束测试：只筛 Role，不筛 UserRole。
     * 若约束误作用于中间表，UserRole 没有 name 字段，pivot 查询将得到 0 条。
     */
    @BelongsToMany(using = UserRole.class, constraints = {
            @Constraint(field = "name", value = "Administrator")
    })
    private List<Role> adminRoles;

    /**
     * 中间表（pivot）静态约束测试：通过 UserRole.id IN (1,3) 过滤中间表。
     * User 1 的 UserRole 为 {1,2,3}，约束后只剩 {1,3}，对应 Role {Administrator, Editor}。
     */
    @BelongsToMany(using = UserRole.class, pivotConstraints = {
            @Constraint(field = "id", type = CriterionType.IN, value = {"1", "3"})
    })
    private List<Role> rolesByPivotIdIn;

    /**
     * 中间模型（through）静态约束测试：通过 Post.title LIKE "Spring" 过滤中间表。
     * User 1 → Post 1 (含 Spring) → Likes {1,2}；User 3 无含 Spring 的 Post → 空。
     */
    @HasManyThrough(through = Post.class, throughConstraints = {
            @Constraint(field = "title", type = CriterionType.LIKE, value = "Spring")
    })
    private List<Likes> springPostLikes;

    /**
     * 中间模型（through）静态约束测试（HasOneThrough）：通过 Profile.description LIKE "Scientist" 过滤中间表。
     * User 5 的 Profile 为 "Data Scientist" → Address 5；其他 User 的 Profile 不匹配 → null。
     */
    @HasOneThrough(through = Profile.class, throughConstraints = {
            @Constraint(field = "description", type = CriterionType.LIKE, value = "Scientist")
    })
    private Address scientistProfileAddress;
}
