package com.biiiiiigmonster.octopus.query.grammars;

import com.biiiiiigmonster.octopus.BaseGrammar;
import com.biiiiiigmonster.octopus.eloquent.Model;
import com.biiiiiigmonster.octopus.query.Builder;

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
