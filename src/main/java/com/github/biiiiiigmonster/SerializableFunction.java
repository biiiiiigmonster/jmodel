package com.github.biiiiiigmonster;

import java.io.Serializable;
import java.util.function.Function;

/**
 *
 * @author v-luyunfeng
 */
@FunctionalInterface
public interface SerializableFunction<T, R> extends Function<T, R>, Serializable {
}
