package com.github.biiiiiigmonster.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.relation.annotation.MorphTo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName
@EqualsAndHashCode(callSuper = false)
public class Comment extends Model<Comment> {
    @TableId
    private Long id;
    private String context;
    private Long commentableId;
    private String commentableType;

    @TableField(exist = false)
    @MorphTo(name = "commentable")
    private Post post;

    @TableField(exist = false)
    @MorphTo(name = "commentable")
    private Video video;
}