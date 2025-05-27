package com.thiru.investment_tracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Configuration
@EnableMongoAuditing(
        auditorAwareRef = "securityAuditorAware",
        dateTimeProviderRef = "utcLocalDateTimeProvider"
)
public class MongoDBConfig {

    // Bean to provide UTC timestamps to audited entities
    @Bean
    public DateTimeProvider utcLocalDateTimeProvider() {
        return () -> Optional.of(LocalDateTime.now(ZoneOffset.UTC));
    }
}
