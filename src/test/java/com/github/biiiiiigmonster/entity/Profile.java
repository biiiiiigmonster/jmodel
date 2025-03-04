package com.github.biiiiiigmonster.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.relation.annotation.BelongsTo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@TableName
@EqualsAndHashCode(callSuper = false)
public class Profile extends Model<Profile> {
    @TableId
    private Long id;
    private BigDecimal balance;
    private Long userId;

    @TableField(exist = false)
    @BelongsTo
    private User user;
}