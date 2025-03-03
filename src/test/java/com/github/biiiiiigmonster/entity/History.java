package com.github.biiiiiigmonster.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.relation.annotation.BelongsTo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName
@EqualsAndHashCode(callSuper = false)
public class History extends Model<History> {
    @TableId
    private Long id;
    private String summary;
    private Long phoneId;

    @TableField(exist = false)
    @BelongsTo
    private Phone phone;
}