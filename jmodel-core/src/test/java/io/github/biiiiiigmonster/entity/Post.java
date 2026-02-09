package io.github.biiiiiigmonster.entity;

import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.relation.annotation.BelongsTo;
import io.github.biiiiiigmonster.relation.annotation.HasMany;
import io.github.biiiiiigmonster.relation.annotation.MorphMany;
import io.github.biiiiiigmonster.relation.annotation.MorphOne;
import io.github.biiiiiigmonster.relation.annotation.MorphToMany;
import io.github.biiiiiigmonster.relation.annotation.config.MorphAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@MorphAlias
public class Post extends Model<Post> {
    private Long id;
    private Long userId;
    private String title;

    @BelongsTo(foreignKey = "userId")
    private User user;

    @HasMany(foreignKey = "postId")
    private List<Likes> likes;

    @MorphOne
    private Image image;

    @MorphMany
    private List<Comment> comments;

    @MorphToMany(using = Taggable.class)
    private List<Tag> tags;
}
