package com.thiru.investment_tracker.migration;

import com.thiru.investment_tracker.dto.enums.CapitalGainsType;
import com.thiru.investment_tracker.entity.ReportEntity;
import com.thiru.investment_tracker.entity.TradeOutcomeEntity;
import com.thiru.investment_tracker.entity.helper.AuditMetadata;
import com.thiru.investment_tracker.repository.ReportRepository;
import com.thiru.investment_tracker.repository.TradeOutcomeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class TradeOutcomeMigration {

    private static final long LONG_TERM_THRESHOLD_DAYS = 365L;

    private final ReportRepository reportRepository;
    private final TradeOutcomeRepository tradeOutcomeRepository;

    /**
     * One-time migration that reads all ReportEntity documents from the 'reports' collection
     * and populates TradeOutcomeEntity in the 'trade_outcomes' collection with best-effort field mapping.
     * <p>
     * Field mapping from ReportEntity to TradeOutcomeEntity:
     * - email, stockCode, stockName, exchangeName, brokerName, assetType, accountType, accountHolder → direct map
     * - purchasePrice → caAdjustedBuyPrice (best-effort: no post-CA adjusted price in old reports)
     * - originalBuyPrice = caAdjustedBuyPrice (best-effort: no original txn price in ReportEntity)
     * - sellPrice → direct map
     * - quantity → sellQuantity (Long cast to Long, note: quantity in ReportEntity was sell qty)
     * - totalValue → totalSellValue (copy existing value, even if buggy/0)
     * - purchaseDate → buyDate
     * - sellDate → direct map
     * - holdingPeriodDays → calculated from ChronoUnit.DAYS.between(buyDate, sellDate)
     * - capitalGainsType → derived from holdingPeriodDays (SHORT_TERM if < 365, else LONG_TERM)
     * - financialYear → derived from sellDate using Indian FY (Apr-Mar)
     * - netProfit → (sellPrice - caAdjustedBuyPrice) * sellQuantity (approximate, no charges)
     * - profitPercentage → (netProfit / totalBuyValue) * 100
     * - isCaDerived → false (cannot determine from old reports)
     * - auditMetadata → copied from ReportEntity or new with migration timestamp
     */
    public void migrateReportsToTradeOutcomes() {
        log.info("Starting migration from ReportEntity to TradeOutcomeEntity");
        LocalDateTime startTime = LocalDateTime.now();

        Iterable<ReportEntity> allReports = reportRepository.findAll();
        int totalCount = 0;
        int skippedCount = 0;
        int migratedCount = 0;

        for (ReportEntity report : allReports) {
            totalCount++;

            // Skip invalid records with missing critical fields
            if (!isValidReport(report)) {
                log.warn("Skipping invalid ReportEntity with id={}: missing required fields", report.getId());
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

    /**
     * Validates that a ReportEntity has the minimum required fields for migration.
     */
    private boolean isValidReport(ReportEntity report) {
        return report != null
                && report.getEmail() != null && !report.getEmail().isBlank()
                && report.getStockCode() != null && !report.getStockCode().isBlank()
                && report.getPurchasePrice() > 0
                && report.getSellPrice() > 0
                && report.getSellQuantity() != null && report.getSellQuantity() > 0
                && report.getPurchaseDate() != null
                && report.getSellDate() != null;
    }

    /**
     * Maps a ReportEntity to TradeOutcomeEntity with best-effort field population.
     */
    private TradeOutcomeEntity mapReportToTradeOutcome(ReportEntity report) {
        TradeOutcomeEntity outcome = new TradeOutcomeEntity();

        // Identity fields - direct mapping
        outcome.setEmail(report.getEmail());
        outcome.setStockCode(report.getStockCode());
        outcome.setStockName(report.getStockName());
        outcome.setExchangeName(report.getExchangeName());
        outcome.setBrokerName(report.getBrokerName());
        outcome.setAssetType(report.getAssetType());
        outcome.setAccountType(report.getAccountType());
        outcome.setAccountHolder(report.getAccountHolder());

        // Buy side - purchasePrice becomes both original and CA-adjusted (best-effort)
        double caAdjustedBuyPrice = report.getPurchasePrice();
        outcome.setOriginalBuyPrice(caAdjustedBuyPrice);
        outcome.setCaAdjustedBuyPrice(caAdjustedBuyPrice);
        outcome.setBuyQuantity(report.getSellQuantity()); // Original qty equals sell qty in ReportEntity
        outcome.setBuyDate(report.getPurchaseDate());
        outcome.setBuyBrokerCharges(0.0); // Not available in ReportEntity
        outcome.setBuyMiscCharges(0.0);   // Not available in ReportEntity

        // Sell side - direct mapping
        outcome.setSellPrice(report.getSellPrice());
        outcome.setSellQuantity(report.getSellQuantity());
        outcome.setSellDate(report.getSellDate());
        outcome.setSellBrokerCharges(0.0); // Not available in ReportEntity
        outcome.setSellMiscCharges(0.0);   // Not available in ReportEntity

        // Computed fields
        double totalBuyValue = caAdjustedBuyPrice * report.getSellQuantity();
        double totalSellValue = report.getTotalValue() > 0
                ? report.getTotalValue()
                : report.getSellPrice() * report.getSellQuantity();
        double netProfit = (report.getSellPrice() - caAdjustedBuyPrice) * report.getSellQuantity();
        double profitPercentage = totalBuyValue > 0 ? (netProfit / totalBuyValue) * 100 : 0.0;

        outcome.setTotalBuyValue(totalBuyValue);
        outcome.setTotalSellValue(totalSellValue);
        outcome.setNetProfit(netProfit);
        outcome.setProfitPercentage(profitPercentage);

        // Holding period and capital gains
        long holdingPeriodDays = ChronoUnit.DAYS.between(report.getPurchaseDate(), report.getSellDate());
        outcome.setHoldingPeriodDays(holdingPeriodDays);
        outcome.setCapitalGainsType(deriveCapitalGainsType(holdingPeriodDays));
        outcome.setFinancialYear(deriveFinancialYear(report.getSellDate()));

        // Linkage fields - not available in ReportEntity
        outcome.setSourceSellTransactionId(null);
        outcome.setSourceBuyLotId(null);

        // CA tracking - cannot determine from old reports
        outcome.setCaDerived(false);

        // Audit metadata - copy from ReportEntity or create new
        outcome.setAuditMetadata(copyOrCreateAuditMetadata(report));

        return outcome;
    }

    /**
     * Derives CapitalGainsType based on holding period.
     * In India, equity held for more than 12 months (365 days) qualifies for long-term capital gains.
     */
    private CapitalGainsType deriveCapitalGainsType(long holdingPeriodDays) {
        return holdingPeriodDays >= LONG_TERM_THRESHOLD_DAYS
                ? CapitalGainsType.LONG_TERM
                : CapitalGainsType.SHORT_TERM;
    }

    /**
     * Derives the Indian Financial Year string (e.g., "FY2023-24") from a sell date.
     * Indian FY runs from April to March.
     */
    private String deriveFinancialYear(LocalDate sellDate) {
        if (sellDate == null) {
            return "UNKNOWN";
        }

        int year = sellDate.getYear();
        int month = sellDate.getMonthValue();

        // If sell date is Jan-Mar, it belongs to FY of previous calendar year
        // e.g., Jan 2024 → FY2023-24
        // If sell date is Apr-Dec, it belongs to FY of current calendar year
        // e.g., Jun 2024 → FY2024-25
        if (month <= 3) {
            return String.format("FY%d-%02d", year - 1, (year % 100));
        } else {
            return String.format("FY%d-%02d", year, ((year + 1) % 100));
        }
    }

    /**
     * Copies AuditMetadata from ReportEntity or creates a new one with migration timestamp.
     */
    private AuditMetadata copyOrCreateAuditMetadata(ReportEntity report) {
        AuditMetadata original = report.getAuditMetadata();
        AuditMetadata newMetadata = new AuditMetadata();

        LocalDateTime now = LocalDateTime.now();

        if (original != null) {
            newMetadata.setCreatedBy(original.getCreatedBy());
            newMetadata.setCreatedAt(original.getCreatedAt());
            newMetadata.setUpdatedBy("TradeOutcomeMigration@" + now);
            newMetadata.setUpdatedAt(now);
        } else {
            newMetadata.setCreatedBy("TradeOutcomeMigration");
            newMetadata.setCreatedAt(now);
            newMetadata.setUpdatedBy("TradeOutcomeMigration");
            newMetadata.setUpdatedAt(now);
        }

        return newMetadata;
    }
}