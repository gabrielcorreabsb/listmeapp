package com.listme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.listme.model")
@EnableJpaRepositories("com.listme.repository")
public class ApiTesteApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiTesteApplication.class, args);
    }
}