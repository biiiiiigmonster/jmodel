package com.biiiiiigmonster.jmodel.eloquent;

import com.biiiiiigmonster.jmodel.contracts.eloquent.EloquentBuilderContract;
import com.biiiiiigmonster.jmodel.query.Builder;

import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/11/21 17:20
 */
public class EloquentBuilder<T> implements EloquentBuilderContract<T> {
    private Builder<T> query;

    private Model<T> model;

    public EloquentBuilder(Builder<T> query) {
        this.query = query;
    }

    public EloquentBuilder<T> setModel(Model<T> model) {
        this.model = model;
        this.query.from(model.getTable(), null);
        return this;
    }

    public List<T> get() {
        return this.query.get();
    }
}
