package com.biiiiiigmonster.jmodel.query;

import com.biiiiiigmonster.jmodel.connections.ConnectionInterface;
import com.biiiiiigmonster.jmodel.contracts.query.BuilderContract;
import com.biiiiiigmonster.jmodel.query.grammars.Grammar;
import com.biiiiiigmonster.jmodel.query.processors.Processor;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/11/21 16:31
 */
public class Builder<T> implements BuilderContract<T> {
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

    public Builder<T> from(String table, String as) {
        this.from = StringUtils.isEmpty(as) ? table : String.format("%s as %s", table, as);
        return this;
    }

    public List<T> get() {
        return this.processor.processSelect(this, this.runSelect());
    }

    protected List<T> runSelect() {
        return this.connection.select(
                this.toSql(),
                this.getBindings(),
                ! this.useWriteDriver
        );
    }

    public String toSql() {
        return this.grammar.compileSelect(this);
    }

    public List<Object> getBindings() {
        return this.bindings;
    }
}
