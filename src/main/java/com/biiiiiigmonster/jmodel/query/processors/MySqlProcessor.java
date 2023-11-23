package com.biiiiiigmonster.jmodel.query.processors;

import com.biiiiiigmonster.jmodel.eloquent.Model;

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
    public <T extends Model<?>> List<T> processColumnListing(List<T> results)
    {
        return results;
    }
}
