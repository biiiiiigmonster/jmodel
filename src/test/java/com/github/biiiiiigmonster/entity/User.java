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
import com.github.biiiiiigmonster.relation.annotation.MorphAlias;
import com.github.biiiiiigmonster.relation.annotation.MorphOne;
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
    private Profile profile;

    @TableField(exist = false)
    @HasOneThrough(through = Profile.class)
    private Address profileAddress;

    @TableField(exist = false)
    @HasMany
    private List<Post> posts;

    @TableField(exist = false)
    @HasManyThrough(through = Post.class)
    private List<Comment> comments;

    @TableField(exist = false)
    @BelongsToMany(using = UserRole.class)
    private List<Role> roles;

    @TableField(exist = false)
    @MorphOne
    private Image image;
}