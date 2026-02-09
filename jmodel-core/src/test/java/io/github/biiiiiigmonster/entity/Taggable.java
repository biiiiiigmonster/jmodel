package io.github.biiiiiigmonster.entity;

import io.github.biiiiiigmonster.relation.MorphPivot;
import io.github.biiiiiigmonster.relation.annotation.config.MorphName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@MorphName
public class Taggable extends MorphPivot<Taggable> {
    private Long id;
    private Long tagId;
    private String taggableType;
    private Long taggableId;
}
