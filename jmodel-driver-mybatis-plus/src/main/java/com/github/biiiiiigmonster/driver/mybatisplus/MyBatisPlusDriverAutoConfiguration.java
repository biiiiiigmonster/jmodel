package com.github.biiiiiigmonster.driver.mybatisplus;

import com.github.biiiiiigmonster.driver.DriverRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * MyBatis-Plus 驱动自动配置类
 * 在 Spring 容器启动时自动注册 MyBatis-Plus 驱动和元数据提供者
 *
 * @author jmodel
 */
@Configuration
@ConditionalOnClass(name = "com.baomidou.mybatisplus.core.mapper.BaseMapper")
public class MyBatisPlusDriverAutoConfiguration {

    @Autowired
    private MyBatisPlusDriver myBatisPlusDriver;

    @Autowired
    private MyBatisPlusMetadata myBatisPlusMetadata;

    @PostConstruct
    public void registerDriver() {
        // 注册 MyBatis-Plus 驱动
        DriverRegistry.registerDriver(MyBatisPlusDriver.class, myBatisPlusDriver);

        // 注册 MyBatis-Plus 元数据提供者
        DriverRegistry.registerMetadata(MyBatisPlusDriver.class, myBatisPlusMetadata);
    }
}
