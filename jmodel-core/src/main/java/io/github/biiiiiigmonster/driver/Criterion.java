package io.github.biiiiiigmonster.driver;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Field;

/**
 * 查询条件项
 */
@Data
@AllArgsConstructor
public class Criterion {
    private final Field field;
    private final CriterionType type;
    private final Object value;
}
