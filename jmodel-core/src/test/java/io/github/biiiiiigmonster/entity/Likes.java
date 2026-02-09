package io.github.biiiiiigmonster.entity;

import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.relation.annotation.BelongsTo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Likes extends Model<Likes> {
    private Long id;
    private Long postId;
    private String praise;

    @BelongsTo
    private Post post;
}
