package io.github.biiiiiigmonster.driver.entity;

import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.relation.annotation.BelongsTo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Organization extends Model<Organization> {
    private Long id;
    private String name;
    private Long regionId;

    @BelongsTo
    private Region region;
}
