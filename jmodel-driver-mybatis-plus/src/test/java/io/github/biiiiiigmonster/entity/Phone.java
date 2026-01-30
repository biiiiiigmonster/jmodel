package io.github.biiiiiigmonster.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.relation.annotation.BelongsTo;
import io.github.biiiiiigmonster.relation.annotation.MorphToMany;
import io.github.biiiiiigmonster.relation.annotation.config.MorphAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@MorphAlias
@TableName
public class Phone extends Model<Phone> {
    @TableId
    private Long id;
    private String number;
    private Long userId;

    @BelongsTo
    @TableField(exist = false)
    private User user;

    @MorphToMany(using = Taggable.class)
    @TableField(exist = false)
    private List<Tag> tags;
}