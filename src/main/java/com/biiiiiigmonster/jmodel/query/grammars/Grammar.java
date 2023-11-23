package com.biiiiiigmonster.jmodel.query.grammars;

import com.biiiiiigmonster.jmodel.BaseGrammar;
import com.biiiiiigmonster.jmodel.eloquent.Model;
import com.biiiiiigmonster.jmodel.query.Builder;

/**
 * <p>
 *
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/11/21 18:04
 */
public class Grammar extends BaseGrammar {
    public <T extends Model<?>> String compileSelect(Builder<T> query) {
        return "select * from " + query.getFrom();
    }
}
