package io.github.biiiiiigmonster.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.relation.annotation.BelongsTo;
import io.github.biiiiiigmonster.relation.annotation.HasMany;
import io.github.biiiiiigmonster.relation.annotation.MorphedByMany;
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
    private Long parentId;

    @TableField(exist = false)
    @BelongsTo(foreignKey = "parentId")
    private Tag parent;

    @TableField(exist = false)
    @HasMany(foreignKey = "parentId")
    private List<Tag> children;

    @TableField(exist = false)
    @MorphedByMany(using = Taggable.class)
    private List<Post> posts;

    @TableField(exist = false)
    @MorphedByMany(using = Taggable.class)
    private List<Video> videos;
}