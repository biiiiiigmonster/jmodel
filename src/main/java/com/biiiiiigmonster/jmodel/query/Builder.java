package com.biiiiiigmonster.octopus.query;

import com.biiiiiigmonster.octopus.connections.ConnectionInterface;
import com.biiiiiigmonster.octopus.contracts.query.BuilderContract;
import com.biiiiiigmonster.octopus.eloquent.Model;
import com.biiiiiigmonster.octopus.query.grammars.Grammar;
import com.biiiiiigmonster.octopus.query.processors.Processor;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
public class Builder<T extends Model<?>> {
    private Class<T> model;
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
        return this.model.getSimpleName().toLowerCase();
    }

    public Builder<T> from(Class<T> entity) {
        this.model = entity;
        return this;
    }

    public List<T> get() {
        return this.processor.processSelect(this, this.runSelect());
    }

    @SneakyThrows
    protected List<T> runSelect() {
        ResultSet rs = this.connection.select(
                this.toSql(),
                this.getBindings(),
                !this.useWriteDriver
        );
        List<T> results = new ArrayList<>();
        while (rs.next()) {
            Field[] fields = this.model.getDeclaredFields();
            T model = this.model.newInstance();
            for (Field field : fields) {
                field.setAccessible(true);
                field.set(model, rs.getObject(field.getName(), field.getType()));
            }
            results.add(model);
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
