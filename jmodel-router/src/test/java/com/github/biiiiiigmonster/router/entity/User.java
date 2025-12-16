package com.github.biiiiiigmonster.router.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.relation.annotation.HasMany;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@TableName
@EqualsAndHashCode(callSuper = false)
public class User extends Model<User> {
    @TableId
    private Long id;
    private String name;
    private String email;

    @TableField(exist = false)
    @HasMany(foreignKey = "userId")
    private List<Post> posts;
}
