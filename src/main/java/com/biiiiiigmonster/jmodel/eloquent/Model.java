package com.biiiiiigmonster.jmodel.eloquent;

import com.biiiiiigmonster.jmodel.connections.Connection;
import com.biiiiiigmonster.jmodel.connections.MysqlConnection;
import com.biiiiiigmonster.jmodel.query.Builder;

/**
 * <p>
 *
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/11/21 16:38
 */
public abstract class Model<T> {
    protected String connection;

    protected String table;

    public EloquentBuilder<T> newQuery() {
        return this.newModelQuery();
    }

    public EloquentBuilder<T> newModelQuery() {
        return this.newEloquentBuilder(
                this.newBaseQueryBuilder()
        ).setModel(this);
    }

    protected Builder<T> newBaseQueryBuilder() {
        return this.getConnection().query();
    }

    public Connection getConnection() {
        return new MysqlConnection(null, null);
    }

    public String getTable() {
        return table;
    }

    public EloquentBuilder<T> newEloquentBuilder(Builder<T> query) {
        return new EloquentBuilder<>(query);
    }
}
