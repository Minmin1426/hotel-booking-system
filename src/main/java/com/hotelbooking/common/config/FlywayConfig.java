package com.hotelbooking.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Flyway migrations.
 */
@Configuration
@Slf4j
public class FlywayConfig {

    /**
     * Custom Flyway migration strategy that runs repair before migration to align
     * schema history checksums.
     */
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            log.info("Running Flyway repair to resolve any checksum mismatches...");
            flyway.repair();
            log.info("Running Flyway migration...");
            flyway.migrate();
        };
    }
}
