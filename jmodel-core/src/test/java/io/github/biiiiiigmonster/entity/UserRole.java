package io.github.biiiiiigmonster.entity;

import io.github.biiiiiigmonster.relation.Pivot;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserRole extends Pivot<UserRole> {
    private Long id;
    private Long userId;
    private Long roleId;
}
