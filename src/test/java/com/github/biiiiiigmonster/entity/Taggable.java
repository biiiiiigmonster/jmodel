package com.github.biiiiiigmonster.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.relation.MorphPivot;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName
@EqualsAndHashCode(callSuper = false)
public class Taggable extends MorphPivot<Taggable> {
    @TableId
    private Long id;
    private Long tagId;
    private Long taggableId;
    private String taggableType;
}