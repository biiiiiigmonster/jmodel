package com.github.biiiiiigmonster.router.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.relation.annotation.BelongsTo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName
@EqualsAndHashCode(callSuper = false)
public class Post extends Model<Post> {
    @TableId
    private Long id;
    private Long userId;
    private String title;

    @TableField(exist = false)
    @BelongsTo(foreignKey = "userId")
    private User user;
}
