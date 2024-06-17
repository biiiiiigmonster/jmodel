package com.biiiiiigmonster.octopus.connections;

import com.biiiiiigmonster.octopus.eloquent.Model;

import java.sql.ResultSet;
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
    ResultSet select(String query, List<Object> bindings, boolean useReadDriver);
}
