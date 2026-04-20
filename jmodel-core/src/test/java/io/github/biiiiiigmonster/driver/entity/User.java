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
import io.github.biiiiiigmonster.relation.scope.SpringInTitleScope;
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
     * 静态 {@code constraint} 类形式
     */
    @HasMany(foreignKey = "userId", constraint = SpringInTitleScope.class)
    private List<Post> scopedSpringPosts;

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
}
