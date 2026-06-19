package com.ddaa.ddaaservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
// Habilita el soporte de caché de Spring.
// La implementación concreta se configura en application.yml usando Redis.
@EnableCaching
public class DdaaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DdaaServiceApplication.class, args);
    }
}