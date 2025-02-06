package com.github.biiiiiigmonster.query;

import com.github.biiiiiigmonster.connections.ConnectionInterface;
import com.github.biiiiiigmonster.query.grammars.Grammar;
import com.github.biiiiiigmonster.query.processors.Processor;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/11/21 16:31
 */
public class Builder<T> {
    private Class<T> entity;
    private String from;
    private Long limit;
    private Long offset;
    private List<Object> bindings;
    private Grammar grammar;
    private ConnectionInterface connection;
    private Processor processor;
    private boolean useWriteDriver;

    public Builder(ConnectionInterface connection, Grammar grammar, Processor processor) {
        this.connection = connection;
        this.grammar = grammar;
        this.processor = processor;
    }

    public String getFrom() {
        return this.from;
    }

    public Builder<T> from(Class<T> entity) {
        this.entity = entity;
        this.from = entity.getSimpleName().toLowerCase();
        return this;
    }

    public List<T> get() {
        return this.processor.processSelect(this, this.runSelect());
    }

    /**
     * Run the query as a “select” statement against the connection.
     */
    @SneakyThrows
    protected List<T> runSelect() {
        ResultSet rs = this.connection.select(
                this.toSql(),
                this.getBindings(),
                !this.useWriteDriver
        );
        List<T> results = new ArrayList<>();
        while (rs.next()) {
            Field[] fields = this.entity.getDeclaredFields();
            T entity = this.entity.newInstance();
            for (Field field : fields) {
                field.setAccessible(true);
                field.set(entity, rs.getObject(field.getName(), field.getType()));
            }
            results.add(entity);
        }
        return results;
    }

    public String toSql() {
        return this.grammar.compileSelect(this);
    }

    public List<Object> getBindings() {
        return this.bindings;
    }
}
