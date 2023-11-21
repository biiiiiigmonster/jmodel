package com.biiiiiigmonster.jmodel.connections;

import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/11/21 20:57
 */
public interface ConnectionInterface {
    public <T> List<T> select(String query, List<Object> bindings, boolean useReadDriver);
}
