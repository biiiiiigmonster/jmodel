package com.github.biiiiiigmonster.model;

import java.io.Serializable;
import java.util.function.Function;

/**
 * <p>
 *
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/10/26 12:39
 */
@FunctionalInterface
public interface SerializableFunction<T, R> extends Function<T, R>, Serializable {
}
