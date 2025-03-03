package com.github.biiiiiigmonster.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.relation.annotation.BelongsTo;
import com.github.biiiiiigmonster.relation.annotation.HasMany;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@TableName
@EqualsAndHashCode(callSuper = false)
public class Phone extends Model<Phone> {
    @TableId
    private Long id;
    private String number;
    private Long userId;
    
    @TableField(exist = false)
    @BelongsTo
    private User user;

    @TableField(exist = false)
    @HasMany
    private List<History> histories;
}