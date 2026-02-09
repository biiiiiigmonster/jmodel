package io.github.biiiiiigmonster.entity;

import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.relation.annotation.MorphTo;
import io.github.biiiiiigmonster.relation.annotation.config.MorphId;
import io.github.biiiiiigmonster.relation.annotation.config.MorphType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Comment extends Model<Comment> {
    private Long id;
    private String content;
    @MorphType
    private String commentableType;
    @MorphId
    private Long commentableId;

    @MorphTo
    private Post post;

    @MorphTo
    private Video video;
}
