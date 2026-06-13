# Step 1 Audit: TradeOutcome Foundation

## Created Files

| File | Path |
|------|------|
| TradeOutcomeEntity | `backend/src/main/java/com/thiru/investment_tracker/entity/TradeOutcomeEntity.java` |
| TradeOutcomeContext | `backend/src/main/java/com/thiru/investment_tracker/dto/context/TradeOutcomeContext.java` |
| TradeOutcomeRepository | `backend/src/main/java/com/thiru/investment_tracker/repository/TradeOutcomeRepository.java` |
| TradeOutcomeService | `backend/src/main/java/com/thiru/investment_tracker/service/TradeOutcomeService.java` |

## Design Decisions

### Collection Name
- `trade_outcomes` — snake_case per project conventions

### Entity Schema

**Identity Fields** (7)
- `email`, `stockCode`, `stockName`, `exchangeName`, `brokerName`, `assetType`, `accountType`, `accountHolder`

**Buy Side** (6)
- `originalBuyPrice` — price before any corporate action adjustments
- `caAdjustedBuyPrice` — price after applying bonus/split/demerger adjustments
- `buyQuantity` — quantity purchased (Long for large volumes)
- `buyDate` — purchase date
- `buyBrokerCharges`, `buyMiscCharges`

**Sell Side** (6)
- `sellPrice`, `sellQuantity`, `sellDate`, `sellBrokerCharges`, `sellMiscCharges`

**Computed Fields** (7)
- `totalBuyValue` — (caAdjustedBuyPrice * buyQuantity) + all buy charges
- `totalSellValue` — (sellPrice * sellQuantity) - all sell charges
- `netProfit` — totalSellValue - totalBuyValue
- `profitPercentage` — (netProfit / totalBuyValue) * 100
- `holdingPeriodDays` — days between buyDate and sellDate
- `capitalGainsType` — SHORT_TERM / LONG_TERM (derived from holdingPeriodDays)
- `financialYear` — FY string e.g. "2024-25"

**Linkage Fields**
- `sourceSellTransactionId` — links to the originating TransactionEntity sell record
- `sourceBuyLotId` — links to the specific buy lot used in FIFO allocation

**CA Tracking**
- `isCaDerived` — boolean flag indicating if this outcome was computed from CA-adjusted data
- `appliedCorporateActions` — list of CorporateActionEntity applied to derive this outcome

### Service Patterns
- `@RequiredArgsConstructor` for DI (no explicit constructors)
- Constructor-injected fields as `private final`
- Uses `TJsonMapper.copy()` for context-to-entity conversion (same as ReportService)
- Two variants for each method: with `UserMail` and with raw `String email`
- `@Log4j2` for logging
- `@Service` annotation

### Repository Patterns
- Extends `MongoRepository<TradeOutcomeEntity, String>`
- `findByEmail(String)` for fetching user outcomes
- `deleteByEmail(String)` for cleanup

### Context vs Entity
- Context uses `@Builder`, `@Getter`, `@Setter`, `@ToString` (mutable, builder-friendly for service layer)
- Entity uses `@Data`, `@AllArgsConstructor`, `@NoArgsConstructor` (JPA-compatible, immutable-ready)
- Conversion done via `TJsonMapper.copy()` — serializes to JSON and deserializes to target class

## Verification
```bash
test -f backend/src/main/java/com/thiru/investment_tracker/entity/TradeOutcomeEntity.java && \
test -f backend/src/main/java/com/thiru/investment_tracker/service/TradeOutcomeService.java && \
echo "PASS"
```