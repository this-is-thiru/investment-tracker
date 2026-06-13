# Reports Collection — Deep Analysis

> Current state, bugs found, and enhancement recommendations to turn `reports` into a powerful analytics source.

---

## 1. Current State

### Schema (`ReportEntity`)

| Field | Type | Populated? | Source |
|---|---|---|---|
| `id` | String | ✅ | Auto |
| `email` | String | ✅ | `userMail.getEmail()` |
| `stock_code` | String | ✅ | `AssetEntity.stockCode` |
| `stock_name` | String | ✅ | `AssetEntity.stockName` |
| `exchange_name` | String | ✅ | `AssetEntity.exchangeName` |
| `broker_name` | BrokerName | ✅ | `AssetEntity.brokerName` |
| `purchase_price` | double | ✅ | `AssetEntity.price` (average buy price of lot) |
| `sell_price` | double | ✅ | `AssetRequest.price` |
| `quantity` | **Long** ⚠️ | ✅ | `sellQuantity` (double cast to Long) |
| `total_value` | double | ❌ **BUG — always 0** | Never set in `toReportContext` |
| `asset_type` | AssetType | ✅ | `AssetEntity.assetType` |
| `account_type` | AccountType | ✅ | `AssetRequest.accountType` |
| `account_holder` | String | ✅ | `AssetRequest.accountHolder` |
| `purchase_date` | LocalDate | ✅ | `AssetEntity.transactionDate` |
| `sell_date` | LocalDate | ✅ | `AssetRequest.transactionDate` |
| `audit_metadata` | AuditMetadata | ✅ | Auto |

### How It Is Created

During `PortfolioService.sellStock()`, for each lot that is (partially or fully) sold:

```java
ReportContext reportContext = toReportContext(assetEntity, assetRequest, sellQuantity);
reportService.stockReport(userMail, reportContext);
```

`ReportService` copies `ReportContext` → `ReportEntity` via `TJsonMapper.copy()`.

### Current Consumers

| Consumer | Usage |
|---|---|
| `ReportController.getReports()` | Returns raw `List<ReportEntity>` by email |
| `PortfolioService.clearAllRecordsForCustomer()` | Deletes all reports for a user |
| `ReportService.updateReports()` | Backfills missing `assetType` (one-time migration) |

> **No analytics or aggregation currently reads from `reports`.**

---

## 2. Bugs Found

### Bug 1: `totalValue` Is Never Set

**Location:** `PortfolioService.toReportContext()` — the line `reportContext.setTotalValue(...)` is **absent**.

**Impact:** Every `ReportEntity` has `totalValue = 0.0`. Any code that tries to use this field gets `0`.

**Fix:** Add to `toReportContext`:
```java
reportContext.setTotalValue(assetRequest.getPrice() * sellQuantity);
```

### Bug 2: `quantity` Is `Long` But Source Is `double`

**Location:** `ReportEntity.quantity` is declared `Long`, but `sellQuantity` in `toReportContext` is a `double`.

**Impact:** Fractional quantities (from demergers, stock splits, or mutual funds) get truncated. e.g., `5.5` → `5`.

**Fix:** Change `ReportEntity.quantity` from `Long` → `Double`.

> ⚠️ **Migration note:** Existing documents with `Long` values in MongoDB will be automatically deserialized to `Double` by Jackson / Spring Data if the field type changes. No data migration script is needed.

---

## 3. What Is Missing (vs. What `ProfitAndLossContext` Already Computes)

During a sell, `ProfitAndLossContext.from()` **already** computes:

| Computed Value | Used In P&L | Stored In Report? |
|---|---|---|
| Pro-rated **buy broker charges** | ✅ Yes | ❌ No |
| Pro-rated **buy misc charges** | ✅ Yes | ❌ No |
| Pro-rated **sell broker charges** | ✅ Yes | ❌ No |
| Pro-rated **sell misc charges** | ✅ Yes | ❌ No |
| **Net gain/loss** `(sell - buy) * qty - charges` | ✅ Yes | ❌ No |
| **Holding period** (> 1 year = LTCG) | ✅ Yes | ❌ No |
| **Financial year** of sale | ✅ Yes | ❌ No |

These values are computed and thrown away after updating `ProfitAndLossEntity`. They are **never** persisted to `ReportEntity`.

---

## 4. Enhancement Recommendations

### Tier 1: Fix Bugs (must-do)

| Change | Field | Action |
|---|---|---|
| Fix | `total_value` | Set `sellPrice * sellQuantity` in `toReportContext` |
| Fix | `quantity` | Change type `Long` → `Double` in `ReportEntity` |

### Tier 2: Add Pre-Computed Analytics Fields (high value, low effort)

These fields can be computed at write-time (during sell) and stored permanently. They enable fast read-time analytics without re-scanning `TransactionEntity`.

| New Field | Type | Formula / Source | Analytical Value |
|---|---|---|---|
| `buy_broker_charges` | double | `(assetEntity.brokerCharges / assetEntity.quantity) * sellQuantity` | Accurate cost basis |
| `buy_misc_charges` | double | `(assetEntity.miscCharges / assetEntity.quantity) * sellQuantity` | Accurate cost basis |
| `sell_broker_charges` | double | `(assetRequest.brokerCharges / assetRequest.quantity) * sellQuantity` | Accurate net inflow |
| `sell_misc_charges` | double | `(assetRequest.miscCharges / assetRequest.quantity) * sellQuantity` | Accurate net inflow |
| `net_profit` | double | `(sellPrice - purchasePrice) * qty - all_charges` | Primary metric for every analytics query |
| `profit_percentage` | double | `netProfit / (purchasePrice * qty + buy_charges) * 100` | Normalized performance comparison |
| `holding_period_days` | int | `ChronoUnit.DAYS.between(purchaseDate, sellDate)` | Avg holding period, turnover analysis |
| `capital_gains_type` | String | `holdingPeriodDays > 365 ? "LONG_TERM" : "SHORT_TERM"` | Portfolio composition by term |
| `financial_year` | String | e.g., `"2024-25"` | Year-over-year comparison |
| `source_sell_transaction_id` | String | `transactionId` (already passed to `sellStock`) | Audit trail, ability to correlate with `TransactionEntity` |

### Changes Required (Minimal Touch Points)

1. **`ReportContext.java`** — Add new fields
2. **`ReportEntity.java`** — Add new `@Field` annotations
3. **`PortfolioService.toReportContext()`** — Populate new fields (charges, profit, days, etc.)
4. **`ReportEntity` constructor/setters** — Lombok `@Data` handles this automatically

No changes needed in:
- `ReportService` (it already copies all matching fields via `TJsonMapper`)
- `ProfitAndLossService` (unaffected)
- Controllers (they return the entity as-is)

### Tier 3: Aggregation Pipeline via Repository (future, not required now)

Once Tier 2 fields are in place, `ReportRepository` can do MongoDB aggregation queries:

```java
// Example: Sum net profit by stock
@Aggregation(pipeline = {
    "{ $match: { email: ?0 } }",
    "{ $group: { _id: '$stock_code', totalProfit: { $sum: '$net_profit' }, count: { $sum: 1 } } }"
})
List<StockProfitAggregation> aggregateProfitByStock(String email);
```

This is **optional** — we can compute aggregations in Java for V1.

---

## 5. Impact on Analytics Features

| Analytics Feature | With Current ReportEntity | With Enhanced ReportEntity |
|---|---|---|
| **Win/Loss count** | Must compute `(sellPrice - purchasePrice) * qty` on every read | Read `netProfit` directly |
| **Avg profit per win** | Must filter + compute on every read | Filter `netProfit > 0`, average pre-computed field |
| **Avg holding period** | Must compute `DAYS.between()` on every read | Average `holdingPeriodDays` directly |
| **Best/Worst stock** | Must group-by + compute on every read | Group-by + sum `netProfit` directly |
| **Portfolio turnover** | Must query `TransactionEntity` for sell values | Sum `total_value` (once fixed) from reports |
| **Short-term vs Long-term split** | Must recompute days on every read | Filter by `capital_gains_type` |
| **Year-over-year P&L** | Must parse dates + group on every read | Group by `financial_year` |
| **XIRR** | Must query `TransactionEntity` (still needed) | No change — XIRR needs full cash flow history |

**Net effect:** Enhanced `ReportEntity` makes **Chunks 2 and 3** of the analytics plan dramatically simpler and faster. It reduces the analytics service from a multi-entity join to simple reads on a single collection.

---

## 6. Open Decision

### Option A: Minimal Fix
Only fix `totalValue` and `quantity` type. Build analytics on top of existing schema (compute at read-time).

**Pros:** Zero data migration, fastest to ship.  
**Cons:** Analytics queries are slower and more complex. Every metric re-computes the same formulas.

### Option B: Enhanced Reports
Add all Tier 2 fields. Reports become a pre-computed analytics fact table.

**Pros:** Simple, fast analytics reads. Single-collection queries. Profit/loss formulas are centralized at write-time.  
**Cons:** Slightly more code in `toReportContext`. Existing reports in DB will have `null` for new fields (backward-compatible — missing fields are `null`).

### Recommendation

**Go with Option B (Enhanced Reports)**. The additional fields are all derivable from data already present at sell-time. The write-time overhead is negligible (a few arithmetic operations). The read-time benefit is enormous — analytics becomes aggregation on a single pre-computed collection.

The `ProfitAndLossContext` already computes pro-rated charges. Re-use that same logic in `toReportContext` to populate the enhanced report fields.

---

## 7. Final Plan Alignment

If we enhance `ReportEntity`, the analytics chunks become:

| Chunk | Files | Effort |
|---|---|---|
| **0. Enhanced Reports** | `ReportEntity`, `ReportContext`, `PortfolioService.toReportContext()` | Small |
| **1. Analytics Skeleton** | Controller + DTOs + empty service | Small |
| **2. Summary + Allocation** | `AnalyticsService` — single-collection reads | Very Small |
| **3. Performance Metrics** | `AnalyticsService` — aggregates on `reports` | Very Small |
| **4. XIRR** | `XirrCalculator` + cash flow construction from `TransactionEntity` | Medium |
| **5. Integration Tests** | `AnalyticsIntegrationTest` | Medium |

Chunk 0 is the foundation that unlocks faster implementation of Chunks 2 and 3.
