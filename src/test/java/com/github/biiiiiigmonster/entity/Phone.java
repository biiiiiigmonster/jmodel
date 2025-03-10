package com.github.biiiiiigmonster.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.relation.annotation.BelongsTo;
import com.github.biiiiiigmonster.relation.annotation.config.MorphAlias;
import com.github.biiiiiigmonster.relation.annotation.MorphToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@MorphAlias
public class Phone extends Model<Phone> {
    private Long id;
    private String number;
    private Long userId;

    @BelongsTo
    private User user;

    @TableField(exist = false)
    @MorphToMany(using = Taggable.class)
    private List<Tag> tags;
}
