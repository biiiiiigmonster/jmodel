package com.github.biiiiiigmonster.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.relation.annotation.config.MorphName;
import com.github.biiiiiigmonster.relation.annotation.MorphTo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName(excludeProperty = "pivot")
@EqualsAndHashCode(callSuper = false)
@MorphName("imageable")
public class Image extends Model<Image> {
    @TableId
    private Long id;
    private String url;
    private String imageableType;
    private Long imageableId;

    @TableField(exist = false)
    @MorphTo
    private User user;

    @TableField(exist = false)
    @MorphTo
    private Post post;
}