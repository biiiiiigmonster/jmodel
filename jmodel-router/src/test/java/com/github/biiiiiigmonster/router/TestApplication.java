package com.github.biiiiiigmonster.router;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.github.biiiiiigmonster.router.mapper")
@ComponentScan(basePackages = {"com.github.biiiiiigmonster.router", "com.github.biiiiiigmonster.relation"})
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
