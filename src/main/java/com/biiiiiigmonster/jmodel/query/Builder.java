package com.biiiiiigmonster.jmodel.query;

import com.biiiiiigmonster.jmodel.connections.ConnectionInterface;
import com.biiiiiigmonster.jmodel.contracts.query.BuilderContract;
import com.biiiiiigmonster.jmodel.eloquent.Model;
import com.biiiiiigmonster.jmodel.query.grammars.Grammar;
import com.biiiiiigmonster.jmodel.query.processors.Processor;
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
public class Builder<T extends Model<?>> implements BuilderContract<T> {
    private Class<T> entity;
    private Long limit;
    private Long offset;
    private List<Object> bindings;
    private Grammar grammar;
    private ConnectionInterface connection;
    private Processor processor;
    private boolean useWriteDriver;

    protected Class<T> currentModelClass() {
        return (Class<T>) getSuperClassGenericType(getClass(), 0);
    }

    public Class<?> getSuperClassGenericType(final Class<?> clazz, final int index) {
        Type genType = clazz.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        if (index >= params.length || index < 0) {
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            return Object.class;
        }
        return (Class<?>) params[index];
    }

    public Builder(ConnectionInterface connection, Grammar grammar, Processor processor) {
        this.connection = connection;
        this.grammar = grammar;
        this.processor = processor;
    }

    public String getFrom() {
        return this.entity.getSimpleName().toLowerCase();
    }

    public Builder<T> from(Class<T> entity) {
        this.entity = entity;
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
            Field[] fields = this.entity.getDeclaredFields();
            T model = this.entity.newInstance();
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
