package com.github.biiiiiigmonster.connections;

import com.github.biiiiiigmonster.eloquent.Model;
import com.github.biiiiiigmonster.query.Builder;
import com.github.biiiiiigmonster.query.grammars.Grammar;
import com.github.biiiiiigmonster.query.processors.Processor;
import lombok.SneakyThrows;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/11/21 21:41
 */
public class Connection implements ConnectionInterface {
    protected java.sql.Connection driver;
    protected Grammar queryGrammar;
    protected Processor postProcessor;
    protected String tablePrefix = "";

    public Connection(java.sql.Connection driver, String tablePrefix) {
        this.driver = driver;
        this.tablePrefix = tablePrefix;
        this.useDefaultQueryGrammar();
        this.useDefaultPostProcessor();
    }

    @Override
    @SneakyThrows
    public ResultSet select(String sql, List<Object> bindings, boolean useReadDriver) {
        Statement statement = this.driver.createStatement();
        return statement.executeQuery(sql);
    }

    public <T extends Model<?>> Builder<T> query() {
        return new Builder<>(this, this.getQueryGrammar(), this.getPostProcessor());
    }

    public Grammar getQueryGrammar() {
        return queryGrammar;
    }

    public Processor getPostProcessor() {
        return postProcessor;
    }

    public void useDefaultQueryGrammar() {
        this.queryGrammar = getDefaultQueryGrammar();
    }

    public void useDefaultPostProcessor() {
        this.postProcessor = getDefaultPostProcessor();
    }

    protected Grammar getDefaultQueryGrammar() {
        return new Grammar();
    }

    protected Processor getDefaultPostProcessor() {
        return new Processor();
    }

    public Grammar withTablePrefix(Grammar grammar) {
        grammar.setTablePrefix(this.tablePrefix);

        return grammar;
    }
}
