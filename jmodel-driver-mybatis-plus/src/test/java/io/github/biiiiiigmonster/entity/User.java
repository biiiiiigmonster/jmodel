package io.github.biiiiiigmonster.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.relation.annotation.BelongsToMany;
import io.github.biiiiiigmonster.relation.annotation.HasMany;
import io.github.biiiiiigmonster.relation.annotation.HasManyThrough;
import io.github.biiiiiigmonster.relation.annotation.HasOne;
import io.github.biiiiiigmonster.relation.annotation.HasOneThrough;
import io.github.biiiiiigmonster.relation.annotation.config.MorphAlias;
import io.github.biiiiiigmonster.relation.annotation.MorphOne;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@TableName
@EqualsAndHashCode(callSuper = false)
@MorphAlias
public class User extends Model<User> {
    @TableId
    private Long id;
    private String name;
    private String email;

    @TableField(exist = false)
    @HasOne
    private Phone phone;

    @TableField(exist = false)
    @HasOne
    private Profile profile;

    @TableField(exist = false)
    @HasOneThrough(through = Profile.class)
    private Address profileAddress;

    @TableField(exist = false)
    @HasMany(foreignKey = Post_.USER_ID)
    private List<Post> posts;

    @TableField(exist = false)
    @HasMany(foreignKey = Post_.USER_ID, chaperone = true)
    private List<Post> postChaperones;

    @TableField(exist = false)
    @HasManyThrough(through = Post.class)
    private List<Likes> commentLikes;

    @TableField(exist = false)
    @BelongsToMany(using = UserRole.class)
    private List<Role> roles;

    @TableField(exist = false)
    @BelongsToMany(using = UserRole.class, withPivot = true)
    private List<Role> roleWithPivots;

    @TableField(exist = false)
    @MorphOne
    private Image image;
}