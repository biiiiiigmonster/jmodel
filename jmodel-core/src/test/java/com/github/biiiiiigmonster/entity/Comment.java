package com.github.biiiiiigmonster.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.relation.annotation.config.Morph;
import com.github.biiiiiigmonster.relation.annotation.MorphTo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName(excludeProperty = "pivot")
@EqualsAndHashCode(callSuper = false)
@Morph(type = "commentableType", id = "commentableId")
public class Comment extends Model<Comment> {
    @TableId
    private Long id;
    private String content;
    private String commentableType;
    private Long commentableId;

    @TableField(exist = false)
    @MorphTo
    private Post post;

    @TableField(exist = false)
    @MorphTo
    private Video video;
}