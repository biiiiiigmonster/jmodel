package io.github.biiiiiigmonster.driver;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 查询条件项
 */
@Data
@AllArgsConstructor
public class Criterion {
    private final String field;
    private final CriterionType type;
    private final Object value;
}
