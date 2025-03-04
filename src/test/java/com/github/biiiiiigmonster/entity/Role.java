package com.github.biiiiiigmonster.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.relation.annotation.BelongsToMany;
import com.github.biiiiiigmonster.relation.annotation.HasManyThrough;
import com.github.biiiiiigmonster.relation.annotation.HasOne;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@TableName
@EqualsAndHashCode(callSuper = false)
public class Role extends Model<Role> {
    @TableId
    private Long id;
    private String name;

    @TableField(exist = false)
    @BelongsToMany(using = UserRole.class)
    private List<User> users;
}