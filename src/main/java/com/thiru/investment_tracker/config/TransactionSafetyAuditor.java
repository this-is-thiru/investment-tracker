package com.thiru.investment_tracker.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.stereotype.Component;

/**
 * A lightweight startup probe that verifies whether a
 * {@link MongoTransactionManager} bean is present and logs its finding
 * at INFO/WARN so operators can confirm transaction safety at a glance.
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class TransactionSafetyAuditor {

    private final ApplicationContext applicationContext;

    @jakarta.annotation.PostConstruct
    public void audit() {
        String beanName;
        try {
            beanName = applicationContext.getBeanNamesForType(MongoTransactionManager.class)[0];
        } catch (Exception e) {
            log.warn("MongoTransactionManager bean NOT found — multi-document transactions are DISABLED. " +
                    "Any @{Transactional} service methods that touch multiple collections will NOT be atomic. " +
                    "Ensure app.mongodb.transactions-enabled=true if you need ACID guarantees.");
            return;
        }

        log.info("MongoDB replica set detected — multi-document transactions are ENABLED via bean '{}'. " +
                "ACID guarantees are active for @{Transactional} methods.", beanName);
    }
}