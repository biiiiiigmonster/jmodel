package io.github.biiiiiigmonster.entity;

import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.relation.annotation.BelongsTo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Address extends Model<Address> {
    private Long id;
    private Long profileId;
    private String location;

    @BelongsTo
    private Profile profile;
}
