// src/main/java/com/example/bigfood/config/FlywayConfig.java
package com.example.bigfood.configuration;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            // Repair nếu cần
            flyway.repair();
            // Migrate
            flyway.migrate();
        };
    }
}