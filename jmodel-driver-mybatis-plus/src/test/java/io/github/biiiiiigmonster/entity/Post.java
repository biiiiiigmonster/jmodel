package io.github.biiiiiigmonster.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.relation.annotation.BelongsTo;
import io.github.biiiiiigmonster.relation.annotation.HasMany;
import io.github.biiiiiigmonster.relation.annotation.config.MorphAlias;
import io.github.biiiiiigmonster.relation.annotation.MorphMany;
import io.github.biiiiiigmonster.relation.annotation.MorphOne;
import io.github.biiiiiigmonster.relation.annotation.MorphToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@TableName
@EqualsAndHashCode(callSuper = false)
@MorphAlias
public class Post extends Model<Post> {
    @TableId
    private Long id;
    private Long userId;
    private String title;

    @TableField(exist = false)
    @BelongsTo(foreignKey = Post_.USER_ID)
    private User user;

    @TableField(exist = false)
    @HasMany(foreignKey = Likes_.POST_ID)
    private List<Likes> likes;

    @TableField(exist = false)
    @MorphOne
    private Image image;

    @TableField(exist = false)
    @MorphMany
    private List<Comment> comments;

    @TableField(exist = false)
    @MorphToMany(using = Taggable.class)
    private List<Tag> tags;
}