package com.thiru.investment_tracker.migration;

import com.thiru.investment_tracker.dto.enums.AccountType;
import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.enums.BrokerName;
import com.thiru.investment_tracker.dto.enums.CapitalGainsType;
import com.thiru.investment_tracker.entity.TradeOutcomeEntity;
import com.thiru.investment_tracker.entity.helper.AuditMetadata;
import com.thiru.investment_tracker.repository.TradeOutcomeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * One-time migration that reads all legacy ReportEntity documents from the 'reports' collection
 * (via MongoTemplate — ReportRepository is deleted) and populates TradeOutcomeEntity in the
 * 'trade_outcomes' collection with best-effort field mapping.
 * <p>
 * Field mapping from ReportEntity (raw BSON) to TradeOutcomeEntity:
 * - email, stockCode, stockName, exchangeName, brokerName, assetType, accountType, accountHolder → direct map
 * - purchasePrice → caAdjustedBuyPrice (best-effort: no post-CA adjusted price in old reports)
 * - originalBuyPrice = caAdjustedBuyPrice (best-effort: no original txn price in ReportEntity)
 * - sellPrice → direct map
 * - quantity → sellQuantity (Long cast to Long; quantity in ReportEntity was sell qty)
 * - totalValue → totalSellValue (copy existing value, even if buggy/0)
 * - purchaseDate → buyDate
 * - sellDate → direct map
 * - holdingPeriodDays → calculated from ChronoUnit.DAYS.between(buyDate, sellDate)
 * - capitalGainsType → derived from holdingPeriodDays (SHORT_TERM if &lt; 365, else LONG_TERM)
 * - financialYear → derived from sellDate using Indian FY (Apr-Mar)
 * - netProfit → (sellPrice - caAdjustedBuyPrice) * sellQuantity (approximate, no charges)
 * - profitPercentage → (netProfit / totalBuyValue) * 100
 * - isCaDerived → false (cannot determine from old reports)
 * - auditMetadata → new with migration timestamp
 * <p>
 * Run once via: GET /migrations/run?className=TradeOutcomeMigration
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TradeOutcomeMigration {

    private static final long LONG_TERM_THRESHOLD_DAYS = 365L;
    private static final String REPORTS_COLLECTION = "reports";

    private final MongoTemplate mongoTemplate;
    private final TradeOutcomeRepository tradeOutcomeRepository;

    public void migrateReportsToTradeOutcomes() {
        log.info("Starting migration from ReportEntity to TradeOutcomeEntity");
        LocalDateTime startTime = LocalDateTime.now();

        // Query the 'reports' collection directly — ReportRepository is deleted
        var allReports = mongoTemplate.findAll(Document.class, REPORTS_COLLECTION);

        int totalCount = 0;
        int skippedCount = 0;
        int migratedCount = 0;

        for (Document report : allReports) {
            totalCount++;

            if (!isValidReport(report)) {
                log.warn("Skipping invalid ReportEntity with id={}: missing required fields",
                        report.getString("_id"));
                skippedCount++;
                continue;
            }

            TradeOutcomeEntity tradeOutcome = mapReportToTradeOutcome(report);
            tradeOutcomeRepository.save(tradeOutcome);
            migratedCount++;

            if (migratedCount % 100 == 0) {
                log.info("Migration progress: processed {} reports, migrated {} trade outcomes",
                        totalCount, migratedCount);
            }
        }

        LocalDateTime endTime = LocalDateTime.now();
        long durationSeconds = ChronoUnit.SECONDS.between(startTime, endTime);

        log.info("Migration completed: total={}, migrated={}, skipped={}, duration={}s",
                totalCount, migratedCount, skippedCount, durationSeconds);
    }

    private boolean isValidReport(Document report) {
        String email = report.getString("email");
        String stockCode = report.getString("stock_code");
        Object purchasePriceObj = report.get("purchase_price");
        Object sellPriceObj = report.get("sell_price");
        Object sellQuantityObj = report.get("sell_quantity");

        double purchasePrice = parseDouble(purchasePriceObj);
        double sellPrice = parseDouble(sellPriceObj);
        long sellQuantity = parseLong(sellQuantityObj);

        Document purchaseDateDoc = report.get("purchase_date", Document.class);
        Document sellDateDoc = report.get("sell_date", Document.class);

        return email != null && !email.isBlank()
                && stockCode != null && !stockCode.isBlank()
                && purchasePrice > 0
                && sellPrice > 0
                && sellQuantity > 0
                && purchaseDateDoc != null
                && sellDateDoc != null;
    }

    private TradeOutcomeEntity mapReportToTradeOutcome(Document report) {
        TradeOutcomeEntity outcome = new TradeOutcomeEntity();

        outcome.setEmail(report.getString("email"));
        outcome.setStockCode(report.getString("stock_code"));
        outcome.setStockName(report.getString("stock_name"));
        outcome.setExchangeName(report.getString("exchange_name"));
        outcome.setBrokerName(parseEnum(BrokerName.class, report.getString("broker_name")));
        outcome.setAssetType(parseEnum(AssetType.class, report.getString("asset_type")));
        outcome.setAccountType(parseEnum(AccountType.class, report.getString("account_type")));
        outcome.setAccountHolder(report.getString("account_holder"));

        double caAdjustedBuyPrice = parseDouble(report.get("purchase_price"));
        outcome.setOriginalBuyPrice(caAdjustedBuyPrice);
        outcome.setCaAdjustedBuyPrice(caAdjustedBuyPrice);
        outcome.setBuyQuantity(parseLong(report.get("sell_quantity")));
        outcome.setBuyDate(parseLocalDate(report.get("purchase_date")));
        outcome.setBuyBrokerCharges(0.0);
        outcome.setBuyMiscCharges(0.0);

        outcome.setSellPrice(parseDouble(report.get("sell_price")));
        outcome.setSellQuantity(parseLong(report.get("sell_quantity")));
        outcome.setSellDate(parseLocalDate(report.get("sell_date")));
        outcome.setSellBrokerCharges(0.0);
        outcome.setSellMiscCharges(0.0);

        long sellQty = parseLong(report.get("sell_quantity"));
        double totalBuyValue = caAdjustedBuyPrice * sellQty;
        double totalSellValue = parseDouble(report.get("total_value"));
        if (totalSellValue <= 0) {
            totalSellValue = parseDouble(report.get("sell_price")) * sellQty;
        }
        double netProfit = (parseDouble(report.get("sell_price")) - caAdjustedBuyPrice) * sellQty;
        double profitPercentage = totalBuyValue > 0 ? (netProfit / totalBuyValue) * 100 : 0.0;

        outcome.setTotalBuyValue(totalBuyValue);
        outcome.setTotalSellValue(totalSellValue);
        outcome.setNetProfit(netProfit);
        outcome.setProfitPercentage(profitPercentage);

        LocalDate buyDate = parseLocalDate(report.get("purchase_date"));
        LocalDate sellDate = parseLocalDate(report.get("sell_date"));
        long holdingPeriodDays = ChronoUnit.DAYS.between(buyDate, sellDate);
        outcome.setHoldingPeriodDays(holdingPeriodDays);
        outcome.setCapitalGainsType(deriveCapitalGainsType(holdingPeriodDays));
        outcome.setFinancialYear(deriveFinancialYear(sellDate));

        outcome.setSourceSellTransactionId(null);
        outcome.setSourceBuyLotId(null);
        outcome.setCaDerived(false);

        outcome.setAuditMetadata(buildAuditMetadata());

        return outcome;
    }

    private double parseDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        if (value instanceof BigDecimal) return ((BigDecimal) value).doubleValue();
        return 0.0;
    }

    private long parseLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number) return ((Number) value).longValue();
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
        return 0L;
    }

    private LocalDate parseLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof java.util.Date) return ((java.util.Date) value).toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        if (value instanceof Document doc) {
            // Spring Data MongoDB stores LocalDate as { "date": "...", "time": "..." } or epoch-based
            Object dateObj = doc.get("date");
            if (dateObj instanceof String s) {
                return LocalDate.parse(s);
            }
            // epoch-based storage
            Number epochDay = doc.get("epochDay", Number.class);
            if (epochDay != null) {
                return LocalDate.ofEpochDay(epochDay.longValue());
            }
            return null;
        }
        return null;
    }

    private CapitalGainsType deriveCapitalGainsType(long holdingPeriodDays) {
        return holdingPeriodDays >= LONG_TERM_THRESHOLD_DAYS
                ? CapitalGainsType.LONG_TERM
                : CapitalGainsType.SHORT_TERM;
    }

    private String deriveFinancialYear(LocalDate sellDate) {
        if (sellDate == null) {
            return "UNKNOWN";
        }
        int year = sellDate.getYear();
        int month = sellDate.getMonthValue();
        if (month <= 3) {
            return String.format("FY%d-%02d", year - 1, (year % 100));
        } else {
            return String.format("FY%d-%02d", year, ((year + 1) % 100));
        }
    }

    private <T extends Enum<T>> T parseEnum(Class<T> enumClass, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Could not parse '{}' as {}, skipping", value, enumClass.getSimpleName());
            return null;
        }
    }

    private AuditMetadata buildAuditMetadata() {
        LocalDateTime now = LocalDateTime.now();
        AuditMetadata metadata = new AuditMetadata();
        metadata.setCreatedBy("TradeOutcomeMigration");
        metadata.setCreatedAt(now);
        metadata.setUpdatedBy("TradeOutcomeMigration@" + now);
        metadata.setUpdatedAt(now);
        return metadata;
    }
}