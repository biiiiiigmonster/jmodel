package io.github.biiiiiigmonster.driver.entity;

import io.github.biiiiiigmonster.relation.Pivot;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Membership extends Pivot<Membership> {
    private Long id;
    private Long userId;
    private Long organizationId;
}
