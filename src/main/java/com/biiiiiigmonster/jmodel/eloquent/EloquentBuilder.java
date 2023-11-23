package com.biiiiiigmonster.jmodel.eloquent;

import com.biiiiiigmonster.jmodel.contracts.eloquent.EloquentBuilderContract;
import com.biiiiiigmonster.jmodel.query.Builder;

import java.lang.reflect.Field;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/11/21 17:20
 */
public class EloquentBuilder<T extends Model<?>> implements EloquentBuilderContract<T> {
    private Builder<T> query;

    private T model;

    public EloquentBuilder(Builder<T> query) {
        this.query = query;
    }

    public EloquentBuilder<T> setModel(T model) {
        this.model = model;
        this.query.from((Class<T>) model.getClass());
        return this;
    }

    public List<T> get() {
        return this.query.get();
    }
}
