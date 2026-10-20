package com.technova.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.technova")
public class TechNovaWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(TechNovaWebApplication.class, args);
    }
}
