package com.thiru.wealthlens.config;

import com.thiru.wealthlens.migration.TransactionBasedTradeOutcomeMigration;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/**
 * On startup, runs the transaction-based migration from TransactionEntity to TradeOutcomeEntity
 * if the 'trade_outcomes' collection is empty. This rebuilds trade outcomes directly from
 * raw BUY/SELL transactions with correct Double quantities (fixing the old ReportEntity Long truncation bug).
 * <p>
 * Safe to run multiple times on startup — will skip if trade_outcomes already has data.
 * To force a re-migration, drop the 'trade_outcomes' collection before restarting the app.
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class TradeOutcomeMigrationRunner implements CommandLineRunner {

    private static final String TRADE_OUTCOMES_COLLECTION = "trade_outcomes";

    private final MongoTemplate mongoTemplate;
    private final TransactionBasedTradeOutcomeMigration migration;

    @Override
    public void run(String... args) {
        if (collectionExists(TRADE_OUTCOMES_COLLECTION) && !isCollectionEmpty(TRADE_OUTCOMES_COLLECTION)) {
            log.info("Collection '{}' already has data — skipping trade outcome migration", TRADE_OUTCOMES_COLLECTION);
            return;
        }

        log.info("Collection '{}' is empty — running transaction-based trade outcome migration", TRADE_OUTCOMES_COLLECTION);
        migration.migrateTransactionsToTradeOutcomes();
        log.info("Transaction-based trade outcome migration completed. Collection now has {} documents",
                mongoTemplate.getCollection(TRADE_OUTCOMES_COLLECTION).countDocuments());
    }

    private boolean collectionExists(String name) {
        return mongoTemplate.collectionExists(name);
    }

    private boolean isCollectionEmpty(String name) {
        return mongoTemplate.getCollection(name).countDocuments() == 0;
    }
}
