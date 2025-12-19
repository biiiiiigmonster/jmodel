package com.github.biiiiiigmonster.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.relation.annotation.config.MorphAlias;
import com.github.biiiiiigmonster.relation.annotation.MorphMany;
import com.github.biiiiiigmonster.relation.annotation.MorphToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@TableName
@EqualsAndHashCode(callSuper = false)
@MorphAlias
public class Video extends Model<Video> {
    @TableId
    private Long id;
    private String title;
    private String url;

    @TableField(exist = false)
    @MorphMany
    private List<Comment> comments;

    @TableField(exist = false)
    @MorphToMany(using = Taggable.class)
    private List<Tag> tags;
}