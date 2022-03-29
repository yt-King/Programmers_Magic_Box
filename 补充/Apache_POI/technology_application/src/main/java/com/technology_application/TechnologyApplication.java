package com.technology_application;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan(basePackages = {"com.technology_application.dao"})
public class TechnologyApplication {

    public static void main(String[] args) {
        SpringApplication.run(TechnologyApplication.class, args);
    }

}
