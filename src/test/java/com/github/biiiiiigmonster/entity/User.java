package com.github.biiiiiigmonster.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.relation.annotation.HasMany;
import com.github.biiiiiigmonster.relation.annotation.HasManyThrough;
import com.github.biiiiiigmonster.relation.annotation.HasOne;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@TableName("user")
@EqualsAndHashCode(callSuper = false)
public class User extends Model<User> {
    @TableId
    private Long id;
    private String name;
    private String email;
    
    @TableField(exist = false)
    @HasOne
    private Phone phone;

    @TableField(exist = false)
    @HasManyThrough(through = Phone.class)
    private List<History> histories;
}