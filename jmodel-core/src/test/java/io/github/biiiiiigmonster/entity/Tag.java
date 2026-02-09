package io.github.biiiiiigmonster.entity;

import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.relation.annotation.BelongsTo;
import io.github.biiiiiigmonster.relation.annotation.HasMany;
import io.github.biiiiiigmonster.relation.annotation.MorphedByMany;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class Tag extends Model<Tag> {
    private Long id;
    private String name;
    private Long parentId;

    @BelongsTo(foreignKey = "parentId")
    private Tag parent;

    @HasMany(foreignKey = "parentId")
    private List<Tag> children;

    @MorphedByMany(using = Taggable.class)
    private List<Post> posts;

    @MorphedByMany(using = Taggable.class)
    private List<Video> videos;
}
