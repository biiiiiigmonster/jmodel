package io.github.biiiiiigmonster.entity;

import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.relation.annotation.BelongsTo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Profile extends Model<Profile> {
    private Long id;
    private String description;
    private Long userId;

    @BelongsTo
    private User user;
}
