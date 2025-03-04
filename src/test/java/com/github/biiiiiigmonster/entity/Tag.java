package com.github.biiiiiigmonster.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.relation.annotation.MorphedByMany;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@TableName
@EqualsAndHashCode(callSuper = false)
public class Tag extends Model<Tag> {
    @TableId
    private Long id;
    private String name;

    @TableField(exist = false)
    @MorphedByMany(using = Taggable.class)
    private List<Post> posts;

    @TableField(exist = false)
    @MorphedByMany(using = Taggable.class)
    private List<Video> videos;
}