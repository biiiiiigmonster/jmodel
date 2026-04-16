package io.github.biiiiiigmonster.driver;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 查询条件项
 */
@Getter
@AllArgsConstructor
public class Criterion {
    private final String field;
    private final CriterionType type;
    private final Object value;
}
