package com.github.biiiiiigmonster.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.relation.annotation.MorphTo;
import com.github.biiiiiigmonster.relation.annotation.config.MorphId;
import com.github.biiiiiigmonster.relation.annotation.config.MorphType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName
@EqualsAndHashCode(callSuper = false)
public class Comment extends Model<Comment> {
    @TableId
    private Long id;
    private String content;
    @MorphType
    private String commentableType;
    @MorphId
    private Long commentableId;

    @TableField(exist = false)
    @MorphTo
    private Post post;

    @TableField(exist = false)
    @MorphTo
    private Video video;
}