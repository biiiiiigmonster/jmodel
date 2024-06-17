package com.biiiiiigmonster.octopus.eloquent;

import com.biiiiiigmonster.octopus.connections.Connection;
import com.biiiiiigmonster.octopus.connections.MysqlConnection;
import com.biiiiiigmonster.octopus.query.Builder;
import lombok.SneakyThrows;

import java.sql.DriverManager;

/**
 * <p>
 *
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/11/21 16:38
 */
public abstract class Model<T extends Model<?>> {
    public EloquentBuilder<T> newQuery() {
        return this.newModelQuery();
    }

    public EloquentBuilder<T> newModelQuery() {
        return this.newEloquentBuilder(
                this.newBaseQueryBuilder()
        ).setModel((T) this);
    }

    protected Builder<T> newBaseQueryBuilder() {
        return this.getConnection().query();
    }

    @SneakyThrows
    public Connection getConnection() {
        Class.forName("com.mysql.cj.jdbc.Driver");
        java.sql.Connection driver = DriverManager.getConnection("jdbc:mysql://localhost:3306/vive?characterEncoding=utf8&autoReconnect=true&failOverReadOnly=false&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true", "root", "root");
        return new MysqlConnection(driver, "");
    }

    public String getTable() {
        return this.getClass().getSimpleName().toLowerCase();
    }

    public EloquentBuilder<T> newEloquentBuilder(Builder<T> query) {
        return new EloquentBuilder<>(query);
    }
}