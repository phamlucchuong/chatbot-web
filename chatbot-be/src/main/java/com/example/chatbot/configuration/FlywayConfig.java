// src/main/java/com/example/chatbot/config/FlywayConfig.java
package com.example.chatbot.configuration;

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