package com.github.biiiiiigmonster.driver.exception;

/**
 * 查询条件异常
 * 当查询条件转换或处理失败时抛出此异常
 * 
 * @author jmodel-core
 */
public class QueryConditionException extends RuntimeException {
    
    private final String conditionType;
    
    /**
     * 构造查询条件异常
     * 
     * @param message 错误消息
     * @param cause 原始异常
     */
    public QueryConditionException(String message, Throwable cause) {
        super("查询条件转换失败: " + message, cause);
        this.conditionType = null;
    }
    
    /**
     * 构造查询条件异常（无原始异常）
     * 
     * @param message 错误消息
     */
    public QueryConditionException(String message) {
        super("查询条件转换失败: " + message);
        this.conditionType = null;
    }
    
    /**
     * 构造查询条件异常（带条件类型）
     * 
     * @param conditionType 条件类型
     * @param message 错误消息
     * @param cause 原始异常
     */
    public QueryConditionException(String conditionType, String message, Throwable cause) {
        super(String.format("查询条件 '%s' 转换失败: %s", conditionType, message), cause);
        this.conditionType = conditionType;
    }
    
    /**
     * 获取条件类型
     * 
     * @return 条件类型，可能为 null
     */
    public String getConditionType() {
        return conditionType;
    }
}
