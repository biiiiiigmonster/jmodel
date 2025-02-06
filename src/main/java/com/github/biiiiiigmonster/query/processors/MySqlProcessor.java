package com.github.biiiiiigmonster.query.processors;

import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/11/21 20:06
 */
public class MySqlProcessor extends Processor {
    public <T> List<T> processColumnListing(List<T> results)
    {
        return results;
    }
}
