package io.github.biiiiiigmonster;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @author luyunfeng
 */
@FunctionalInterface
public interface SerializableFunction<T, R> extends Function<T, R>, Serializable {
}
