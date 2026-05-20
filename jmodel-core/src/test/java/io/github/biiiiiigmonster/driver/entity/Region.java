package io.github.biiiiiigmonster.driver.entity;

import io.github.biiiiiigmonster.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Region extends Model<Region> {
    private Long id;
    private String name;
}
