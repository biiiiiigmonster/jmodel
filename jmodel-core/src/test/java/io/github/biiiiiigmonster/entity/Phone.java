package io.github.biiiiiigmonster.entity;

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
public class Phone extends Model<Phone> {
    private Long id;
    private String number;
    private Long userId;

    @BelongsTo
    private User user;

    @MorphToMany(using = Taggable.class)
    private List<Tag> tags;
}
