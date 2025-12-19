package com.github.biiiiiigmonster;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.github.biiiiiigmonster.mapper")
@ComponentScan(basePackages = {"com.github.biiiiiigmonster", "com.github.biiiiiigmonster.driver.mybatisplus"})
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}