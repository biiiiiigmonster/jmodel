package com.github.biiiiiigmonster.driver.exception;

/**
 * 驱动操作异常
 * 当驱动执行操作失败时抛出此异常
 * 
 * @author jmodel-core
 */
public class DriverOperationException extends RuntimeException {
    
    private final String operation;
    private final String driverName;
    
    /**
     * 构造驱动操作异常
     * 
     * @param operation 操作名称
     * @param driverName 驱动名称
     * @param cause 原始异常
     */
    public DriverOperationException(String operation, String driverName, Throwable cause) {
        super(String.format("驱动 '%s' 执行操作 '%s' 失败", driverName, operation), cause);
        this.operation = operation;
        this.driverName = driverName;
    }
    
    /**
     * 构造驱动操作异常（无原始异常）
     * 
     * @param operation 操作名称
     * @param driverName 驱动名称
     * @param message 错误消息
     */
    public DriverOperationException(String operation, String driverName, String message) {
        super(String.format("驱动 '%s' 执行操作 '%s' 失败: %s", driverName, operation, message));
        this.operation = operation;
        this.driverName = driverName;
    }
    
    /**
     * 获取操作名称
     * 
     * @return 操作名称
     */
    public String getOperation() {
        return operation;
    }
    
    /**
     * 获取驱动名称
     * 
     * @return 驱动名称
     */
    public String getDriverName() {
        return driverName;
    }
}
