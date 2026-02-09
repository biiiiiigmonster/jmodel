package io.github.biiiiiigmonster.entity;

import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.relation.annotation.MorphMany;
import io.github.biiiiiigmonster.relation.annotation.MorphToMany;
import io.github.biiiiiigmonster.relation.annotation.config.MorphAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@MorphAlias
public class Video extends Model<Video> {
    private Long id;
    private String title;
    private String url;

    @MorphMany
    private List<Comment> comments;

    @MorphToMany(using = Taggable.class)
    private List<Tag> tags;
}
