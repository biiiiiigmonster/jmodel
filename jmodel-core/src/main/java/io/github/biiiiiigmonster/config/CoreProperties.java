package io.github.biiiiiigmonster.config;

import io.github.biiiiiigmonster.driver.DataDriver;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JModel Core 配置属性
 *
 * @author jmodel-core
 */
@Data
@Component
@ConfigurationProperties(prefix = "jmodel.core")
public class CoreProperties {
    /**
     * 模型配置
     */
    private Model model = new Model();

    /**
     * 驱动配置
     */
    private Driver driver;

    /**
     * 模型配置类
     */
    @Data
    public static class Model {

        /**
         * 默认驱动类
         */
        private String defaultPrimaryKey = "id";
    }

    /**
     * 驱动配置类
     */
    @Data
    public static class Driver {

        /**
         * 默认驱动类
         */
        private Class<? extends DataDriver> defaultDriver;
    }
}
