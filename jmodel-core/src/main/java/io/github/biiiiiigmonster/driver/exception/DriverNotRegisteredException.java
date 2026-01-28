package io.github.biiiiiigmonster.driver.exception;

/**
 * 驱动未注册异常
 * 当尝试获取未注册的驱动时抛出此异常
 * 
 * @author jmodel-core
 */
public class DriverNotRegisteredException extends RuntimeException {
    
    private final String driverName;
    private final Class<?> modelClass;
    
    /**
     * 构造驱动未注册异常
     * 
     * @param driverName 驱动名称
     * @param modelClass 模型类
     */
    public DriverNotRegisteredException(String driverName, Class<?> modelClass) {
        super(String.format("驱动 '%s' 未注册，模型类: %s", driverName, modelClass.getName()));
        this.driverName = driverName;
        this.modelClass = modelClass;
    }
    
    /**
     * 构造驱动未注册异常（仅驱动名称）
     * 
     * @param driverName 驱动名称
     */
    public DriverNotRegisteredException(String driverName) {
        super(String.format("驱动 '%s' 未注册", driverName));
        this.driverName = driverName;
        this.modelClass = null;
    }
    
    /**
     * 获取驱动名称
     * 
     * @return 驱动名称
     */
    public String getDriverName() {
        return driverName;
    }
    
    /**
     * 获取模型类
     * 
     * @return 模型类，可能为 null
     */
    public Class<?> getModelClass() {
        return modelClass;
    }
}
