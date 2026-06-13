# Step 2 Audit: TradeOutcomeMigration

**Date:** 2026-06-14
**Step:** 2/4 - Build one-time migration/backfill script
**Status:** Complete

## Purpose

Create a Spring `@Service` component that migrates historical data from `ReportEntity` (buggy collection) to `TradeOutcomeEntity` (clean target schema).

## Source Schema: ReportEntity

```java
@Document(value = "reports")
public class ReportEntity {
    String id;
    String email;
    String stockCode;
    String stockName;
    String exchangeName;
    BrokerName brokerName;
    double purchasePrice;
    double sellPrice;
    Long sellQuantity;           // Note: named sellQuantity but represents original buy qty
    double totalValue;           // Buggy: often 0 in old reports
    AssetType assetType;
    AccountType accountType;
    String accountHolder;
    LocalDate purchaseDate;
    LocalDate sellDate;
    AuditMetadata auditMetadata;
}
```

## Target Schema: TradeOutcomeEntity

```java
@Document(value = "trade_outcomes")
public class TradeOutcomeEntity {
    String id;
    // Identity fields (7): email, stockCode, stockName, exchangeName, brokerName, assetType, accountType, accountHolder
    // Buy side (6): originalBuyPrice, caAdjustedBuyPrice, buyQuantity, buyDate, buyBrokerCharges, buyMiscCharges
    // Sell side (5): sellPrice, sellQuantity, sellDate, sellBrokerCharges, sellMiscCharges
    // Computed (6): totalBuyValue, totalSellValue, netProfit, profitPercentage, holdingPeriodDays, capitalGainsType, financialYear
    // Linkage (2): sourceSellTransactionId, sourceBuyLotId
    // CA tracking (2): isCaDerived, appliedCorporateActions
    AuditMetadata auditMetadata;
}
```

## Field Mapping Strategy

| ReportEntity Field | TradeOutcomeEntity Field | Mapping Notes |
|--------------------|--------------------------|---------------|
| email | email | Direct map |
| stockCode | stockCode | Direct map |
| stockName | stockName | Direct map |
| exchangeName | exchangeName | Direct map |
| brokerName | brokerName | Direct map |
| assetType | assetType | Direct map |
| accountType | accountType | Direct map |
| accountHolder | accountHolder | Direct map |
| purchasePrice | originalBuyPrice, caAdjustedBuyPrice | Best-effort: both set to purchasePrice (no post-CA data in old reports) |
| sellPrice | sellPrice | Direct map |
| sellQuantity | sellQuantity, buyQuantity | ReportEntity.qty is Long, set both sell and buy quantity |
| totalValue | totalSellValue | Best-effort: if 0, fallback to sellPrice * sellQuantity |
| purchaseDate | buyDate | Direct map |
| sellDate | sellDate | Direct map |
| N/A | holdingPeriodDays | Calculated: ChronoUnit.DAYS.between(buyDate, sellDate) |
| N/A | capitalGainsType | Derived: SHORT_TERM if < 365 days, else LONG_TERM |
| N/A | financialYear | Derived: Indian FY (Apr-Mar), e.g., "FY2024-25" |
| N/A | netProfit | Calculated: (sellPrice - caAdjustedBuyPrice) * sellQuantity |
| N/A | profitPercentage | Calculated: (netProfit / totalBuyValue) * 100 |
| N/A | sourceSellTransactionId | Not available in ReportEntity → set to null |
| N/A | sourceBuyLotId | Not available in ReportEntity → set to null |
| N/A | isCaDerived | Cannot determine → set to false |
| N/A | appliedCorporateActions | Cannot determine → set to empty list |
| N/A | buyBrokerCharges, buyMiscCharges | Not available → set to 0.0 |
| N/A | sellBrokerCharges, sellMiscCharges | Not available → set to 0.0 |
| auditMetadata | auditMetadata | Copied from source; updatedBy set to "TradeOutcomeMigration@{timestamp}" |

## Validation Rules

Records are skipped if they have missing or invalid:
- email (required)
- stockCode (required)
- purchasePrice <= 0
- sellPrice <= 0
- sellQuantity is null or <= 0
- purchaseDate is null
- sellDate is null

## Known Limitations

1. **No post-CA adjusted prices:** ReportEntity.purchasePrice is the original buy price. We cannot determine if corporate actions adjusted it later, so we set both originalBuyPrice and caAdjustedBuyPrice to the same value.

2. **No broker/misc charges:** TradeOutcomeEntity has buyBrokerCharges, buyMiscCharges, sellBrokerCharges, sellMiscCharges, but ReportEntity has none. All set to 0.0.

3. **No transaction linkage:** sourceSellTransactionId and sourceBuyLotId are null. These would be needed for tracing back to source transactions.

4. **isCaDerived = false:** Historical reports don't indicate whether they were derived from corporate action processing.

5. **Profit calculation approximation:** netProfit = (sellPrice - caAdjustedBuyPrice) * sellQuantity ignores charges and CA adjustments.

## Implementation Notes

- Migration is idempotent-friendly: re-running will create duplicates with different `id` fields (MongoDB generates new `_id`). Consider adding a unique constraint on (email, stockCode, buyDate, sellDate) if dedup is needed.
- Logs progress every 100 records for observability.
- Uses `@Service @Log4j2 @RequiredArgsConstructor` following project conventions.

## Next Steps

After running this migration:
1. Verify data in `trade_outcomes` collection
2. Update PortfolioService to write to TradeOutcomeService
3. Delete ReportEntity, ReportContext, ReportService, ReportRepository
4. Implement analytics endpoints that read from TradeOutcomeEntity