package com.github.biiiiiigmonster.query.processors;

import com.github.biiiiiigmonster.query.Builder;

import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/11/21 19:52
 */
public class Processor {
    public <T> List<T> processSelect(Builder<T> builder, List<T> results) {
        return results;
    }

    public <T> List<T> processColumnListing(List<T> results)
    {
        return results;
    }
}
