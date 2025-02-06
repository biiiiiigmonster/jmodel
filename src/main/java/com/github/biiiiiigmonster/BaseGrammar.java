package com.github.biiiiiigmonster;

/**
 * <p>
 *
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/11/21 17:30
 */
public abstract class BaseGrammar {
    protected String tablePrefix = "";

    protected boolean isJsonSelector(String value) {
        return value.contains("->");
    }

    public String quoteString(String value)
    {
        return String.format("'%s'", value);
    }

    public BaseGrammar setTablePrefix(String prefix)
    {
        this.tablePrefix = prefix;

        return this;
    }
}
