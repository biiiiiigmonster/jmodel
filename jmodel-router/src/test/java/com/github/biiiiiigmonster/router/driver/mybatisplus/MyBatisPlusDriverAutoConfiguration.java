package com.github.biiiiiigmonster.router.driver.mybatisplus;

import com.github.biiiiiigmonster.driver.DriverRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * MyBatis-Plus 驱动自动配置类
 * 在 Spring 容器启动时自动注册 MyBatis-Plus 驱动和元数据提供者
 * 
 * @author jmodel-core
 */
@Configuration
public class MyBatisPlusDriverAutoConfiguration {
    
    @Autowired
    private MyBatisPlusDriver myBatisPlusDriver;
    
    @Autowired
    private MyBatisPlusMetadata myBatisPlusMetadata;
    
    /**
     * 在 Bean 初始化后自动注册驱动和元数据提供者
     */
    @PostConstruct
    public void registerDriver() {
        // 注册 MyBatis-Plus 驱动
        DriverRegistry.registerDriver(MyBatisPlusDriver.DRIVER_NAME, myBatisPlusDriver);
        
        // 注册 MyBatis-Plus 元数据提供者
        DriverRegistry.registerMetadata(MyBatisPlusMetadata.PROVIDER_NAME, myBatisPlusMetadata);
        
        // 设置为默认驱动
        DriverRegistry.setDefaultDriver(MyBatisPlusDriver.DRIVER_NAME);
    }
}
