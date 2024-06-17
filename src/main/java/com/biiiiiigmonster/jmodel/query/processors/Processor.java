package com.biiiiiigmonster.octopus.query.processors;

import com.biiiiiigmonster.octopus.eloquent.Model;
import com.biiiiiigmonster.octopus.query.Builder;

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
    public <T extends Model<?>> List<T> processSelect(Builder<T> builder, List<T> results) {
        return results;
    }

    public <T extends Model<?>> List<T> processColumnListing(List<T> results)
    {
        return results;
    }
}
