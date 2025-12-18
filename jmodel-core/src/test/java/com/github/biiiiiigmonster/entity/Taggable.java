package com.github.biiiiiigmonster.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.biiiiiigmonster.relation.MorphPivot;
import com.github.biiiiiigmonster.relation.annotation.config.MorphName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName
@EqualsAndHashCode(callSuper = false)
@MorphName
public class Taggable extends MorphPivot<Taggable> {
    @TableId
    private Long id;
    private Long tagId;
    private String taggableType;
    private Long taggableId;
}