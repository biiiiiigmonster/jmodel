package io.github.biiiiiigmonster.driver.exception;

import io.github.biiiiiigmonster.driver.DataDriver;
import lombok.Getter;

/**
 * 驱动操作异常
 * 当驱动执行操作失败时抛出此异常
 * 
 * @author jmodel-core
 */
@Getter
public class DriverOperationException extends RuntimeException {
    
    private final String operation;
    private final Class<? extends DataDriver> driverClass;
    
    /**
     * 构造驱动操作异常
     * 
     * @param operation 操作名称
     * @param driverClass 驱动类
     * @param cause 原始异常
     */
    public DriverOperationException(String operation, Class<? extends DataDriver> driverClass, Throwable cause) {
        super(String.format("驱动 '%s' 执行操作 '%s' 失败", driverClass, operation), cause);
        this.operation = operation;
        this.driverClass = driverClass;
    }
    
    /**
     * 构造驱动操作异常（无原始异常）
     * 
     * @param operation 操作名称
     * @param driverClass 驱动类
     * @param message 错误消息
     */
    public DriverOperationException(String operation, Class<? extends DataDriver> driverClass, String message) {
        super(String.format("驱动 '%s' 执行操作 '%s' 失败: %s", driverClass, operation, message));
        this.operation = operation;
        this.driverClass = driverClass;
    }
}
