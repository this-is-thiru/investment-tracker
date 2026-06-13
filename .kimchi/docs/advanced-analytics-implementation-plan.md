# Advanced Analytics — Implementation Plan (V1)

> Scope: Self-contained analytics using **only existing DB data**. No external APIs, no sector data, no benchmarking, no persistent data-model changes.
> All features compute on-demand from `TransactionEntity`, `AssetEntity`, `ReportEntity`, and `ProfitAndLossEntity`.

---

## What Is In Scope

| # | Feature | Input (existing) | New Code |
|---|---|---|---|
| 1 | **Portfolio Summary** | `AssetEntity`, `ProfitAndLossEntity`, `TransactionEntity` | `AnalyticsController`, `AnalyticsService`, DTOs |
| 2 | **Asset Allocation** | `AssetEntity` grouped by `AssetType` | Same service method |
| 3 | **Performance Metrics** | `ReportEntity`, `TransactionEntity`, `AssetEntity` | Same service method |
| 4 | **XIRR (realized + optional current holdings)** | `TransactionEntity` cash flows; optional `currentPrices` from request body | `XirrCalculator` utility, `AnalyticsService` method |

## What Is Out of Scope

- External market-price APIs (Yahoo Finance, Alpha Vantage, NSE)
- Sector allocation (needs sector field on entities)
- Unrealized P&L dashboard (unless caller provides prices at request time)
- Benchmark comparison (Nifty 50)
- Pre-computed analytics snapshots / caching
- Chart-library-specific JSON shapes (we emit clean chart-agnostic JSON)

---

## Chunk 1: Analytics Controller + DTOs + Service Skeleton

**Files:**
- `controller/AnalyticsController.java`
- `service/AnalyticsService.java`
- `dto/PortfolioSummaryResponse.java`
- `dto/AssetAllocationResponse.java`
- `dto/PerformanceMetricsResponse.java`
- `dto/XirrRequest.java`
- `dto/XirrResponse.java`
- `dto/MarketPriceEntry.java`

**Goal:** Wire up the controller with 4 GET endpoints and empty service methods that return stub data.

**Endpoints:**
```
GET /analytics/user/{email}/portfolio-summary
GET /analytics/user/{email}/asset-allocation
GET /analytics/user/{email}/performance-metrics
POST /analytics/user/{email}/xirr
```

**Acceptance criteria:**
- All endpoints return 200 with stub DTOs.
- `POST /xirr` accepts `XirrRequest` body with optional `currentPrices` list.
- DTOs follow existing conventions (`@Data @NoArgsConstructor`, `AuditableResponse` if applicable).

---

## Chunk 2: Portfolio Summary + Asset Allocation

**Files:**
- `service/AnalyticsService.java` — implement `getPortfolioSummary()` and `getAssetAllocation()`
- `dto/PortfolioSummaryResponse.java`
- `dto/AssetAllocationResponse.java`

**Goal:** Compute summary numbers purely from existing collections.

### Portfolio Summary Logic

```java
public PortfolioSummaryResponse getPortfolioSummary(UserMail userMail) {
    List<AssetEntity> assets = portfolioRepository.findByEmail(userMail.getEmail());
    List<TransactionEntity> transactions = transactionRepository.findByEmail(userMail.getEmail());
    Optional<ProfitAndLossEntity> latestPnL = profitAndLossRepository
        .findByEmailOrderByLastUpdatedTimeDesc(userMail.getEmail()) // may need new query method
        .stream().findFirst();

    double totalInvested = assets.stream()
        .mapToDouble(a -> a.getPrice() * a.getQuantity())
        .sum();

    double totalRealizedProfit = latestPnL
        .map(pnl -> pnl.getRealisedProfits().getTotal()) // or manual aggregation
        .orElse(0.0);

    long totalTrades = transactions.size();
    long buyTrades = transactions.stream().filter(t -> t.getTransactionType() == BUY).count();
    long sellTrades = transactions.stream().filter(t -> t.getTransactionType() == SELL).count();

    return PortfolioSummaryResponse.builder()
        .totalInvested(totalInvested)
        .totalRealizedProfit(totalRealizedProfit)
        .totalTrades(totalTrades)
        .buyTrades(buyTrades)
        .sellTrades(sellTrades)
        .holdingCount(assets.stream().filter(a -> a.getQuantity() > 0).count())
        .build();
}
```

> **Note:** `ProfitAndLossEntity` stores realized profits by financial year. For a total across all years we may need a new repository query `findByEmail` and sum across records, or add a `findAllByEmail` and compute in Java. The `RealisedProfits` object contains `shortTermCapitalGains` and `longTermCapitalGains` which each have a `totalProfit` field. We need to verify the exact field names.

### Asset Allocation Logic

```java
public List<AssetAllocationResponse> getAssetAllocation(UserMail userMail) {
    List<AssetEntity> assets = portfolioRepository.findByEmail(userMail.getEmail());
    Map<AssetType, Double> investedByType = assets.stream()
        .collect(Collectors.groupingBy(
            AssetEntity::getAssetType,
            Collectors.summingDouble(a -> a.getPrice() * a.getQuantity())
        ));

    double total = investedByType.values().stream().mapToDouble(Double::doubleValue).sum();

    return investedByType.entrySet().stream()
        .map(e -> new AssetAllocationResponse(e.getKey(), e.getValue(), e.getValue() / total * 100))
        .toList();
}
```

**Acceptance criteria:**
- `GET /analytics/user/{email}/portfolio-summary` returns correct numbers for a test user.
- `GET /analytics/user/{email}/asset-allocation` returns percentages summing to 100.
- Unit tests pass for both methods.

---

## Chunk 3: Performance Metrics

**Files:**
- `service/AnalyticsService.java` — implement `getPerformanceMetrics()`
- `dto/PerformanceMetricsResponse.java`

**Goal:** Compute win/loss, averages, turnover, best/worst stock from `ReportEntity`.

### Data Available in ReportEntity
- `purchasePrice`, `sellPrice`, `sellQuantity`, `purchaseDate`, `sellDate`
- `stockCode`, `stockName`, `brokerName`

### Metrics to Compute

| Metric | Formula |
|---|---|
| **Total trades (sells)** | `reportRepository.findByEmail(email).size()` |
| **Win count** | Reports where `sellPrice > purchasePrice` |
| **Loss count** | Reports where `sellPrice < purchasePrice` |
| **Breakeven count** | Reports where `sellPrice == purchasePrice` |
| **Avg profit per win** | Average of `(sellPrice - purchasePrice) * sellQuantity` for wins |
| **Avg loss per loss** | Average of `(purchasePrice - sellPrice) * sellQuantity` for losses |
| **Win/Loss ratio** | `winCount / lossCount` (or 0 if no losses) |
| **Avg holding period** | Average days between `purchaseDate` and `sellDate` |
| **Portfolio turnover** | `totalSellValue / totalInvestedValue` (annualized ideally, but simple version first) |
| **Best performing stock** | Stock with max `((sellPrice - purchasePrice) / purchasePrice) * 100` |
| **Worst performing stock** | Stock with min of the same |

### Implementation Sketch

```java
public PerformanceMetricsResponse getPerformanceMetrics(UserMail userMail) {
    List<ReportEntity> reports = reportRepository.findByEmail(userMail.getEmail());

    List<ReportEntity> wins = reports.stream()
        .filter(r -> r.getSellPrice() > r.getPurchasePrice()).toList();
    List<ReportEntity> losses = reports.stream()
        .filter(r -> r.getSellPrice() < r.getPurchasePrice()).toList();

    double avgProfitPerWin = wins.stream()
        .mapToDouble(r -> (r.getSellPrice() - r.getPurchasePrice()) * r.getSellQuantity())
        .average().orElse(0.0);

    double avgLossPerLoss = losses.stream()
        .mapToDouble(r -> (r.getPurchasePrice() - r.getSellPrice()) * r.getSellQuantity())
        .average().orElse(0.0);

    // ... similar for holding period, turnover, best/worst
}
```

**Acceptance criteria:**
- All metrics computed correctly against known test data.
- Edge cases handled: empty reports, all wins, all losses.
- Unit tests with Mockito stubs for `ReportRepository`.

---

## Chunk 4: XIRR Calculator

**Files:**
- `util/calculator/XirrCalculator.java`
- `dto/XirrRequest.java`
- `dto/XirrResponse.java`
- `dto/CashFlow.java`
- `service/AnalyticsService.java` — implement `calculateXirr()`

**Goal:** Pure Java utility that computes XIRR using Newton-Raphson.

### XirrCalculator Utility

```java
public final class XirrCalculator {
    private static final double EPSILON = 1e-7;
    private static final int MAX_ITERATIONS = 100;
    private static final double INITIAL_GUESS = 0.1;

    private XirrCalculator() {}

    public static double calculate(List<CashFlow> cashFlows) {
        // Newton-Raphson
        double r = INITIAL_GUESS;
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            double npv = npv(cashFlows, r);
            if (Math.abs(npv) < EPSILON) {
                return r;
            }
            double derivative = npvDerivative(cashFlows, r);
            if (derivative == 0) {
                throw new IllegalArgumentException("XIRR derivative is zero — cannot converge");
            }
            r = r - npv / derivative;
        }
        throw new IllegalArgumentException("XIRR did not converge within " + MAX_ITERATIONS + " iterations");
    }

    private static double npv(List<CashFlow> cashFlows, double r) {
        LocalDate baseDate = cashFlows.getFirst().getDate();
        return cashFlows.stream()
            .mapToDouble(cf -> cf.getAmount() / Math.pow(1 + r, daysBetween(baseDate, cf.getDate()) / 365.0))
            .sum();
    }

    private static double npvDerivative(List<CashFlow> cashFlows, double r) {
        LocalDate baseDate = cashFlows.getFirst().getDate();
        return cashFlows.stream()
            .mapToDouble(cf -> {
                double days = daysBetween(baseDate, cf.getDate()) / 365.0;
                return -days * cf.getAmount() / Math.pow(1 + r, days + 1);
            })
            .sum();
    }
}
```

### Cash Flow Construction

From `TransactionEntity` for a user (or filtered by `stockCode`):

```java
List<CashFlow> cashFlows = new ArrayList<>();

for (TransactionEntity txn : transactions) {
    double amount = txn.getPrice() * txn.getQuantity() + txn.getBrokerCharges() + txn.getMiscCharges();
    double signedAmount = switch (txn.getTransactionType()) {
        case BUY -> -amount;
        case SELL -> amount; // SELL is inflow, but charges reduce it; existing ReportContext uses sell raw
    };
    cashFlows.add(new CashFlow(signedAmount, txn.getTransactionDate()));
}
```

> **Open item:** Should SELL cash flow deduct brokerCharges from the inflow? The `ReportEntity` stores raw sell price. For accurate XIRR, the SELL cash flow should be `sellPrice * quantity - brokerCharges - miscCharges` (net inflow). We may need to read from `TransactionEntity` rather than `ReportEntity` to get charges on the sell side. The `TransactionEntity` already stores `brokerCharges` and `miscCharges`.

### Optional Current Holdings

If the request body contains `currentPrices`:

```json
{
  "currentPrices": [
    { "stockCode": "INFY", "price": 1850.50 }
  ]
}
```

Add a final cash flow:
```java
for (AssetEntity asset : assets) {
    if (asset.getQuantity() > 0) {
        Double currentPrice = findPrice(request.getCurrentPrices(), asset.getStockCode());
        if (currentPrice != null) {
            cashFlows.add(new CashFlow(currentPrice * asset.getQuantity(), LocalDate.now()));
        }
    }
}
```

If no `currentPrices` provided and holdings exist, the response should include a flag:
```json
{
  "xirr": 0.1875,
  "xirrPercentage": 18.75,
  "includesOpenPositions": false,
  "message": "Open positions excluded. Provide currentPrices for full XIRR."
}
```

**Acceptance criteria:**
- XIRR matches expected value for a known cash-flow series (e.g., Excel XIRR function).
- Converges within 100 iterations for typical portfolios.
- Returns meaningful error message for non-converging or invalid inputs (e.g., all inflows or all outflows).
- Unit tests cover: simple two-flow, multiple buys + one sell, non-convergent edge case.

---

## Chunk 5: Integration Tests

**Files:**
- `AnalyticsIntegrationTest.java` (extends `AbstractIntegrationTest`)

**Goal:** End-to-end tests for all 4 endpoints using Testcontainers MongoDB.

**Test scenarios:**
1. Seed transactions → call portfolio-summary → assert totals.
2. Seed assets of mixed types → call asset-allocation → assert percentages.
3. Seed reports → call performance-metrics → assert win/loss counts.
4. Seed buy/sell transactions → call XIRR → assert ~expected rate.

**Acceptance criteria:**
- All integration tests pass with `./mvnw test -pl backend -Dtest=AnalyticsIntegrationTest`.

---

## Testing Strategy

| Layer | Approach |
|---|---|
| **Unit** | `@ExtendWith(MockitoExtension.class)`, mock repositories, test calculations in isolation |
| **Integration** | Testcontainers MongoDB, REST Assured, seed data via repository saves |
| **XIRR accuracy** | Compare against Excel/Google Sheets XIRR for the same cash flows |

---

## No New Dependencies

Everything uses existing stack:
- XIRR math → `java.lang.Math`
- Aggregation → `java.util.stream`
- Testing → JUnit 5 + Mockito + Testcontainers (already in pom)

---

## Risks & Mitigations

| Risk | Mitigation |
|---|---|
| ReportEntity missing broker charges for sells | Use `TransactionEntity` for XIRR cash flows instead of `ReportEntity` |
| Newton-Raphson fails to converge for some portfolios | Fallback to bisection method; return error if both fail |
| Very large transaction history causes slow on-demand compute | Add `@Indexed` on `TransactionEntity.email` if not already present |
| `ProfitAndLossEntity` stores year-by-year data but no total | Compute total by iterating all years in service layer |

---

## Order of Execution

| Order | Chunk | Why This Order |
|---|---|---|
| 1 | Skeleton (controller + DTOs + empty service) | Defines the contract for all subsequent work |
| 2 | Portfolio Summary + Asset Allocation | Simplest calculations; validates data access patterns |
| 3 | Performance Metrics | Builds on ReportEntity; slightly more complex aggregations |
| 4 | XIRR Calculator + Endpoint | Most complex math; builds on cash-flow construction from earlier chunks |
| 5 | Integration Tests | Validates the full flow end-to-end |

> **Parallel opportunity:** Chunks 2 and 3 are independent after chunk 1 and could be built in parallel if using subagents. Chunk 4 depends on the cash-flow construction understanding from chunks 2–3 but is mostly independent math. Chunk 5 must come last.
