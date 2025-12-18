package com.github.biiiiiigmonster.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.relation.annotation.BelongsToMany;
import com.github.biiiiiigmonster.relation.annotation.HasMany;
import com.github.biiiiiigmonster.relation.annotation.HasManyThrough;
import com.github.biiiiiigmonster.relation.annotation.HasOne;
import com.github.biiiiiigmonster.relation.annotation.HasOneThrough;
import com.github.biiiiiigmonster.relation.annotation.config.MorphAlias;
import com.github.biiiiiigmonster.relation.annotation.MorphOne;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@TableName(excludeProperty = "pivot")
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