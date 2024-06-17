package com.biiiiiigmonster.octopus.connections;

import com.biiiiiigmonster.octopus.eloquent.Model;
import com.biiiiiigmonster.octopus.query.grammars.MysqlGrammar;

/**
 * <p>
 *
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/11/21 21:47
 */
public class MysqlConnection extends Connection {
    public MysqlConnection(java.sql.Connection driver, String tablePrefix) {
        super(driver, tablePrefix);
    }

    @Override
    protected MysqlGrammar getDefaultQueryGrammar() {
        return (MysqlGrammar) this.withTablePrefix(new MysqlGrammar());
    }
}
