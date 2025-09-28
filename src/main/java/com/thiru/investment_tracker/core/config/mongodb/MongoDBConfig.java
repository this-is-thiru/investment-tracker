package com.thiru.investment_tracker.core.config.mongodb;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing(
        auditorAwareRef = "securityAuditorAware"
//        dateTimeProviderRef = "utcLocalDateTimeProvider"
)
public class MongoDBConfig {

//    // Bean to provide UTC timestamps to audited entities
//    @Bean
//    public DateTimeProvider utcLocalDateTimeProvider() {
//        return () -> Optional.of(LocalDateTime.now(ZoneOffset.UTC));
//    }
}
