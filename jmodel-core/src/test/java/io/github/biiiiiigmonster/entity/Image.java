package io.github.biiiiiigmonster.entity;

import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.relation.annotation.MorphTo;
import io.github.biiiiiigmonster.relation.annotation.config.MorphName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@MorphName("imageable")
public class Image extends Model<Image> {
    private Long id;
    private String url;
    private String imageableType;
    private Long imageableId;

    @MorphTo
    private User user;

    @MorphTo
    private Post post;
}
