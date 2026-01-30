package io.github.biiiiiigmonster;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("io.github.biiiiiigmonster.mapper")
@ComponentScan(basePackages = {"io.github.biiiiiigmonster", "io.github.biiiiiigmonster.driver.mybatisplus"})
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}