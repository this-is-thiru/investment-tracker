# Advanced Analytics: Detailed Analysis

> Scope: Portfolio performance metrics, XIRR calculation, and sector/asset allocation charts.

---

## 1. Portfolio Performance Metrics

### 1.1 What Metrics Are Feasible Today

| Metric | Feasibility | Data Source | Notes |
|---|---|---|---|
| **Total Invested Value** | ✅ Ready | `AssetEntity` (BUY transactions) | Sum of `price * quantity` for all holdings |
| **Total Realized P&L** | ✅ Ready | `ProfitAndLossEntity` | Already aggregated by financial year |
| **Total Unrealized P&L** | ⚠️ Partial | `AssetEntity` + external price | Need current market price for each holding |
| **Win / Loss Ratio** | ✅ Ready | `ReportEntity` / `ProfitAndLossEntity` | Count of profitable vs loss-making sells |
| **Average Holding Period** | ✅ Ready | `AssetEntity.transactionDate` | Days between buy and sell (or today for open) |
| **CAGR (per stock / overall)** | ⚠️ Needs XIRR | `TransactionEntity` | Best computed via XIRR for irregular cash flows |
| **Portfolio Turnover** | ✅ Ready | `TransactionEntity` | Total sell value / average portfolio value |
| **Max Drawdown** | ❌ Needs history | N/A | Requires historical portfolio snapshots |
| **Sharpe / Sortino Ratio** | ❌ Needs returns history | N/A | Requires periodic return data over time |

### 1.2 Recommended V1 Metrics

1. **Portfolio Summary Card**
   - Total Invested
   - Current Portfolio Value (invested ± unrealized P&L)
   - Total Realized Profit/Loss
   - Total Unrealized Profit/Loss
   - Overall Return %

2. **Transaction-level Metrics**
   - Number of trades (total BUY + SELL)
   - Win count / Loss count / Breakeven count
   - Average profit per winning trade
   - Average loss per losing trade
   - Average holding period (days)

3. **Time-series Ready Metrics**
   - Portfolio value per month (this requires computing holdings at month-end)

### 1.3 Key Gap: Current Market Prices

> **Critical dependency:** Unrealized P&L and current portfolio value require the **current market price** of each holding.

- The app currently stores `AssetEntity.price` as the **purchase price** (average for the lot).
- There is no field for `currentMarketPrice`.
- **Options:**
  1. **User-provided** — add a manual update endpoint (`POST /portfolio/user/{email}/market-price`) where the user periodically updates current prices.
  2. **External API** — integrate with NSE/BSE free APIs or a data provider (Alpha Vantage, Yahoo Finance) to fetch live prices by `stockCode`.
  3. **Hybrid** — store a `lastKnownPrice` with a `lastPriceUpdatedAt` timestamp; refresh via API when stale.

---

## 2. XIRR (Extended Internal Rate of Return)

### 2.1 Why XIRR (not simple CAGR)

- The app has **irregular cash flows** — users buy/sell at different dates, quantities, and prices.
- Simple CAGR assumes a single lump-sum investment. XIRR handles:
  - Multiple buy dates (SIPs, staggered purchases)
  - Partial sells
  - Dividends (if tracked later)
- XIRR is the standard metric for portfolio performance in the Indian mutual fund and equity space.

### 2.2 Algorithm: Newton-Raphson Method

XIRR solves for the rate `r` such that:

```
NPV = Σ (cashFlow_i / (1 + r)^((date_i - date_0) / 365)) = 0
```

**Newton-Raphson iteration:**
```
r_next = r_current - NPV(r) / NPV'(r)
```

Where `NPV'(r)` is the derivative of NPV with respect to `r`.

**Convergence criteria:**
- Stop when `|NPV| < 0.0001` or after 100 iterations.
- Initial guess: `r = 0.1` (10%).

### 2.3 Cash Flow Construction Rules

For a user's portfolio, construct cash flows from `TransactionEntity`:

| Event | Cash Flow Sign | Amount | Date |
|---|---|---|---|
| **BUY** | Negative (outflow) | `-(price * quantity + brokerCharges + miscCharges)` | `transactionDate` |
| **SELL** | Positive (inflow) | `+(price * quantity - brokerCharges - miscCharges)` | `transactionDate` |
| **Current Holdings** | Positive (notional inflow) | `+(currentMarketPrice * remainingQuantity)` | `today` |

**Important nuances:**
- Include **broker charges and misc charges** in cash flows for accuracy.
- For current holdings, use today's date with the current market price as a "hypothetical sell."
- Corporate actions (bonus, split) do not create cash flows — they only adjust quantity/price. The existing CA logic in `PortfolioService` already handles quantity adjustments, so XIRR can use the post-CA quantities directly from `AssetEntity`.

### 2.4 Per-Stock XIRR vs Portfolio XIRR

| Level | Use Case | Data |
|---|---|---|
| **Per-stock** | Analyze which stocks performed best | All BUY/SELL for that `stockCode` + current holding |
| **Portfolio-level** | Overall wealth growth | All transactions across all stocks + all current holdings |

### 2.5 Edge Cases

- **All sells, no holdings:** No "today" cash flow needed; XIRR uses historical buys and sells only.
- **No sells, only holdings:** Multiple negative buys + one positive "today" holding.
- **Same-day buy and sell:** Use `transactionDate` (no time component). If same date, both cash flows count.
- **XIRR fails to converge:** Fallback to a simple annualized return: `(totalValue / totalInvestment)^(365/days) - 1`.

---

## 3. Sector / Asset Allocation Charts

### 3.1 Asset Allocation (Ready Today)

The app already has `AssetType`:
```java
EQUITY, MUTUAL_FUND, BOND, GOLD_BOND, FD, INSURANCE
```

This is sufficient for **asset class allocation charts** with zero changes to the data model.

**Example output for a pie chart:**
```json
{
  "assetAllocation": [
    { "assetType": "EQUITY", "investedValue": 500000, "percentage": 62.5 },
    { "assetType": "MUTUAL_FUND", "investedValue": 200000, "percentage": 25.0 },
    { "assetType": "GOLD_BOND", "investedValue": 100000, "percentage": 12.5 }
  ]
}
```

### 3.2 Sector Allocation (Requires New Data)

> **Critical gap:** There is no sector information anywhere in the app.

**Sector mapping options (ranked by effort vs value):**

| Option | Effort | Accuracy | Maintenance |
|---|---|---|---|
| **A. User-provided sector field** | Low | High | User-managed |
| **B. Static CSV/JSON mapping file** | Medium | Medium | Manual update when IPOs list |
| **C. External API (NSE/BSE stock info)** | Medium-High | High | API-dependent |
| **D. AI/LLM classification by stock name** | Medium | Variable | API cost |

**Recommended approach for V1: Option A + B hybrid**
1. Add a `sector` field to `AssetRequest` / `AssetEntity` (optional, user-provided).
2. Ship a static `stock-sector-mapping.json` for top ~500 NSE/BSE stocks.
3. When a transaction is added:
   - If user provides sector, use it.
   - Else, look up in static mapping.
   - Else, default to `"UNKNOWN"`.

**Sector taxonomy (suggested, aligned with NSE/BSE):**
```
BANKING_AND_FINANCE, IT_AND_SOFTWARE, PHARMA_AND_HEALTHCARE,
ENERGY_AND_OIL, AUTOMOBILE, CONSUMER_GOODS, INFRASTRUCTURE,
METALS_AND_MINING, TELECOM, REAL_ESTATE, CHEMICALS, FMCG,
DEFENSE, MEDIA_AND_ENTERTAINMENT, UNKNOWN
```

### 3.3 Chart Data Format

The backend should return JSON that any frontend charting library (Chart.js, Recharts, ApexCharts) can consume directly.

**Sector allocation response:**
```json
{
  "sectorAllocation": [
    { "sector": "IT_AND_SOFTWARE", "investedValue": 300000, "currentValue": 450000, "percentage": 37.5 },
    { "sector": "BANKING_AND_FINANCE", "investedValue": 200000, "currentValue": 220000, "percentage": 25.0 }
  ]
}
```

**Asset + sector combined (drill-down ready):**
```json
{
  "allocation": {
    "EQUITY": {
      "totalInvested": 500000,
      "sectors": {
        "IT_AND_SOFTWARE": 300000,
        "BANKING_AND_FINANCE": 200000
      }
    }
  }
}
```

---

## 4. Proposed API Endpoints

### 4.1 Analytics Controller

```
GET  /analytics/user/{email}/portfolio-summary
GET  /analytics/user/{email}/performance-metrics
GET  /analytics/user/{email}/xirr
GET  /analytics/user/{email}/asset-allocation
GET  /analytics/user/{email}/sector-allocation
POST /analytics/user/{email}/market-prices        ← bulk update current prices
```

### 4.2 Endpoint Details

#### `GET /analytics/user/{email}/portfolio-summary`

Returns a quick overview card.

```json
{
  "totalInvested": 800000.00,
  "currentPortfolioValue": 950000.00,
  "totalRealizedProfit": 45000.00,
  "totalUnrealizedProfit": 105000.00,
  "overallReturnPercentage": 18.75,
  "totalTrades": 42,
  "winningTrades": 28,
  "losingTrades": 14,
  "lastUpdated": "2025-06-13T10:00:00"
}
```

#### `GET /analytics/user/{email}/performance-metrics`

Returns detailed metrics.

```json
{
  "averageHoldingPeriodDays": 245,
  "averageProfitPerWin": 12500.00,
  "averageLossPerLoss": 3200.00,
  "winLossRatio": 2.0,
  "portfolioTurnover": 0.35,
  "bestPerformingStock": { "stockCode": "INFY", "returnPercentage": 45.2 },
  "worstPerformingStock": { "stockCode": "RELIANCE", "returnPercentage": -8.5 }
}
```

#### `GET /analytics/user/{email}/xirr`

Supports per-stock and portfolio-level.

```bash
GET /analytics/user/{email}/xirr?stockCode=INFY     # per stock
GET /analytics/user/{email}/xirr                     # portfolio level
```

```json
{
  "xirr": 0.1875,
  "xirrPercentage": 18.75,
  "calculationDate": "2025-06-13",
  "cashFlowsCount": 15,
  "converged": true
}
```

#### `GET /analytics/user/{email}/asset-allocation`

Zero new data needed.

#### `GET /analytics/user/{email}/sector-allocation`

Requires `sector` field on assets.

#### `POST /analytics/user/{email}/market-prices`

Bulk update current market prices (enables unrealized P&L).

```json
{
  "prices": [
    { "stockCode": "INFY", "currentPrice": 1850.50 },
    { "stockCode": "RELIANCE", "currentPrice": 2950.00 }
  ]
}
```

### 4.3 New DTOs / Entities Needed

| Class | Type | Purpose |
|---|---|---|
| `PortfolioSummaryResponse` | DTO | Summary card data |
| `PerformanceMetricsResponse` | DTO | Detailed metrics |
| `XirrResponse` | DTO | XIRR result + metadata |
| `AssetAllocationResponse` | DTO | Asset class pie chart data |
| `SectorAllocationResponse` | DTO | Sector pie chart data |
| `MarketPriceUpdateRequest` | DTO | Bulk price update input |
| `MarketPrice` | Embedded | `stockCode` + `currentPrice` + `updatedAt` |
| `AnalyticsService` | Service | Core calculation logic |
| `XirrCalculator` | Utility | Pure math, static methods |

### 4.4 Data Model Changes

**Minimal changes to existing entities:**

```java
// AssetEntity.java — add:
@Field("current_market_price")
private Double currentMarketPrice;

@Field("last_price_updated_at")
private LocalDateTime lastPriceUpdatedAt;

@Field("sector")
private String sector;   // optional, for sector allocation
```

```java
// AssetRequest.java — add:
private String sector;   // optional user input
```

---

## 5. Implementation Approaches

### 5.1 Approach A: On-Demand Computation (Recommended for V1)

- Compute all analytics when the endpoint is called.
- Read `TransactionEntity` and `AssetEntity` from DB.
- Run calculations in-memory.
- **Pros:** Simple, always up-to-date, no schema migration needed for computed fields.
- **Cons:** Slower for users with thousands of transactions (acceptable for V1).

### 5.2 Approach B: Pre-Computed + Cached (Future)

- Maintain an `AnalyticsSnapshot` collection that stores pre-computed metrics.
- Update it asynchronously after every transaction (via RabbitMQ event or `@EventListener`).
- Read from snapshot for API responses.
- **Pros:** Instant API response.
- **Cons:** More complex, eventual consistency, requires event plumbing.

### 5.3 Recommended Roadmap

| Phase | Scope | Effort |
|---|---|---|
| **P1** | Asset allocation endpoint + portfolio summary (no new data) | Small |
| **P2** | XIRR calculator (utility) + XIRR endpoint (portfolio + per-stock) | Medium |
| **P3** | Performance metrics (win/loss, avg holding, turnover) | Small |
| **P4** | Market price update endpoint + unrealized P&L | Small |
| **P5** | Sector field + sector allocation endpoint + static mapping | Medium |
| **P6** | Pre-computed analytics snapshot + caching | Medium |

---

## 6. Challenges & Considerations

### 6.1 Corporate Actions and XIRR

- Bonus issues, stock splits, and demergers change quantity/price but not cash flow.
- The existing `PortfolioService` already adjusts `AssetEntity.quantity` and `AssetEntity.price` post-CA.
- **Recommendation:** XIRR should use the **post-CA adjusted** `AssetEntity` lots to build cash flows. Read `AssetEntity.orderTimeQuantities` for the exact buy history.

### 6.2 Mutual Funds and XIRR

- Mutual funds often have SIP-like irregular purchases.
- XIRR handles this perfectly.
- For mutual fund current value, use NAV × units.

### 6.3 Performance for Large Portfolios

- A user with 10,000+ transactions would cause on-demand computation to be slow.
- **Mitigation:** Limit V1 to on-demand. Add DB-level indexing on `TransactionEntity.email + transactionDate`.
- **Long-term:** Move to pre-computed snapshots (Phase P6).

### 6.4 External API Rate Limits

- If choosing external APIs for live prices or sector data, be mindful of rate limits.
- **Mitigation:** Cache prices with TTL (e.g., 15 minutes during market hours, 1 day after hours).

### 6.5 No New Dependencies Needed (for V1)

- XIRR → pure Java math (`java.lang.Math`)
- Asset allocation → aggregation on existing data
- Sector allocation → needs only the `sector` String field (no external library)

---

## 7. Open Questions for Discussion

1. **Current market prices:** Should we integrate an external API (Yahoo Finance, Alpha Vantage, NSE official API) or build a manual price update UI first?
2. **Sector data:** Should sector be user-provided, auto-fetched via API, or a static mapping file?
3. **Charting:** Will the frontend use a specific charting library? (This affects JSON response shape — e.g., Chart.js vs Recharts expect slightly different label/value structures.)
4. **Scope of V1:** Should we ship P1–P4 (asset allocation, XIRR, metrics, market prices) together, or split into smaller PRs?
5. **Currency:** All prices are INR today. Any plan for multi-currency support?
6. **Mutual fund NAVs:** Should NAV fetching be treated the same as stock prices, or is there a separate AMFI/MF source?
7. **Benchmarking:** Should we compare portfolio XIRR against a benchmark (e.g., Nifty 50)? This requires benchmark historical data.
