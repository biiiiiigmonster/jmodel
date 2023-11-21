package com.biiiiiigmonster.jmodel.connections;

import com.biiiiiigmonster.jmodel.query.Builder;
import com.biiiiiigmonster.jmodel.query.grammars.Grammar;
import com.biiiiiigmonster.jmodel.query.processors.Processor;

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
        this.useDefaultQueryGrammar();
        this.useDefaultPostProcessor();
    }

    @Override
    public <T> List<T> select(String query, List<Object> bindings, boolean useReadDriver) {
        return null;
    }

    public <T> Builder<T> query() {
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
