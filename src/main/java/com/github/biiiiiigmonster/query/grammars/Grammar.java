package com.github.biiiiiigmonster.query.grammars;

import com.github.biiiiiigmonster.BaseGrammar;
import com.github.biiiiiigmonster.query.Builder;

/**
 * <p>
 *
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/11/21 18:04
 */
public class Grammar extends BaseGrammar {

    /**
     * Compile a select query into SQL.
     */
    public <T> String compileSelect(Builder<T> query) {
        return "select * from " + query.getFrom();
    }
}
