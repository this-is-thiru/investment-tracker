# Corporate Actions Impact on Analytics — Corrected Analysis

> Honest assessment: CAs were not fully considered in the earlier reports-vs-transactions recommendation. Here is the corrected analysis.

---

## How Each CA Modifies Data

### BONUS

```java
// Bonus creates a NEW AssetEntity with price=0
AssetEntity bonusSharesEntity = TJsonMapper.copy(firstAsset, AssetEntity.class);
bonusSharesEntity.setQuantity((double) bonusSharesCount);
bonusSharesEntity.setPrice(0);  // ← ZERO price
```

Also creates a `TransactionEntity` with `price=0, totalValue=0`.

**Original buy lots are UNCHANGED.** They keep their original `price` and `quantity`.

### STOCK_SPLIT

**Deprecated method** (`processStockSplit(CorporateActionDto)`):
```java
assetEntity.setQuantity(previousQuantity * quantityMultiplier);
assetEntity.setPrice(assetEntity.getPrice() * priceMultiplier);
// Also modifies TransactionEntity in-place
```

**Active method** (incomplete in code — has commented-out sections, returns `null`).

Key issue: **Historical records are mutated in-place.** A buy of 10 shares @ 1000 becomes 20 shares @ 500 after 1:1 split. The original purchase price is lost.

### DEMERGER

```java
// Original stock: price reduced
entity.setPrice(oldPrice * (mainStockPricePercentage / 100));

// New demerged stock created
AssetEntity asNewEntity = asNewEntity(demergerStockContext, entity);
asNewEntity.setPrice(demergerContext.pricePercentage() * (entity.getPrice() / 100));
```

Original stock's price is reduced; a new stock entity is created with a derived price. Both are correct for their respective P&L, but the **original cost basis is fragmented across two entities**.

---

## Impact on Each Analytics Feature

| Feature | Data Source | CA Impact |
|---|---|---|
| **Portfolio Summary** (invested, realized P&L) | `AssetEntity` + `ProfitAndLossEntity` | ✅ Safe. AssetEntity already reflects CA adjustments. P&L already handles CAs during sell. |
| **Asset Allocation** | `AssetEntity` grouped by type | ✅ Safe. CAs don't change asset type. |
| **XIRR** | `TransactionEntity` ⚠️ | 🔴 **Critical.** XIRR MUST use raw cash flows. AssetEntity post-CA prices are NOT actual cash outflows. `ReportEntity` is also wrong for XIRR because it stores post-CA purchasePrice. |
| **Win/Loss ratio** | `AssetEntity` or `ReportEntity` | ⚠️ Post-CA prices ARE correct for P&L. A bonus lot with `price=0` correctly yields profit = sellPrice * qty. But "avg purchase price" per stock becomes misleading. |
| **Avg holding period** | `AssetEntity` / `ReportEntity` | ⚠️ Bonus lots have `transactionDate = exDate`, so holding period for bonus component is counted from ex-date. This is correct for LTCG but may confuse analytics. |
| **Best/worst stock** | Summed `ReportEntity` / reconstructed trades | ⚠️ Demerger fragments cost basis across two stock codes. "Worst stock" might show the demerged entity as bad when the combined position was fine. |
| **Portfolio turnover** | Total sell value / invested | ⚠️ Demerger changes invested value (single stock → two stocks). Turnover calculation must handle this. |

---

## The Critical Insight

### `AssetEntity.price` is NOT your original cost basis after a stock split

```
User buys 10 shares @ 1000 on 2022-01-01   → Transaction: 10@1000
2023-06-01: 1:1 stock split
AssetEntity becomes: 20 shares @ 500      ← original 1000 is LOST
2024-01-01: Sells 20 shares @ 600

ReportEntity: purchasePrice = 500 (post-split)
              sellPrice = 600
              profit = 100 * 20 = 2000

But original cost basis was 10,000.
Post-split adjusted basis is also 10,000 (20 * 500).
So P&L is correct.

BUT for analytics like "total return %":
  Using TransactionEntity: (12000 - 10000) / 10000 = 20%
  Using ReportEntity:     (12000 - 10000) / 10000 = 20% ✓

For XIRR:
  Need cash flows: -10000 on 2022-01-01, +12000 on 2024-01-01 ✓
  ReportEntity captures neither cash flow correctly.
```

Post-CA prices are **mathematically equivalent** for P&L calculation but **semantically different** for analytics that care about original cost basis or actual cash flows.

---

## Why ReportEntity / TradeOutcome Is Risky for XIRR

A `TradeOutcome` stores:
- `buyPrice` = post-CA adjusted price from `AssetEntity`
- `sellPrice` = sell transaction price

For XIRR, we need:
- ACTUAL cash outflow on buy date (from `TransactionEntity`)
- ACTUAL cash inflow on sell date (from `TransactionEntity`)

**XIRR MUST be built from `TransactionEntity`, NOT from any post-CA fact table.**

---

## Why ReportEntity Is Also Risky for Performance Metrics

### Example: Bonus Shares

```
Buy 10 shares @ 1000 on 2022-01-01
Bonus 1:1 on 2023-06-01 → get 10 bonus shares @ 0
Sell 20 shares @ 1500 on 2024-01-01

FIFO allocation:
  Lot 1 (original): 10 shares @ 1000 → sold first
  Lot 2 (bonus):   10 shares @ 0    → sold second

Reports created:
  Report 1: purchasePrice=1000, sellPrice=1500, qty=10  → profit = 5000
  Report 2: purchasePrice=0,    sellPrice=1500, qty=10  → profit = 15000

Total profit = 20000 (correct)

BUT: User sees "one report with purchasePrice=0" and thinks it's a data bug.
Analytics should flag: "This lot came from a corporate action (BONUS)."
```

### Example: Demerger

```
Buy 10 shares @ 1000 on 2022-01-01
Demerger on 2023-06-01: 70% to main stock, 30% to new stock
Sell 10 main shares @ 800 on 2024-01-01
Sell 10 new shares @ 400 on 2024-01-01

AssetEntity for main: 10 shares @ 700 (1000 * 0.70)
AssetEntity for new:  10 shares @ 300 (1000 * 0.30)

Report for main: purchasePrice=700, sellPrice=800, qty=10 → profit = 1000
Report for new:  purchasePrice=300, sellPrice=400, qty=10 → profit = 1000

Total profit = 2000 (correct)
BUT: Best/worst stock analysis treats them as separate positions.
User might think "main stock only gave 14% return" without realizing 
the 43% return from the new stock was part of the same original investment.
```

### Problem Summary for Reports

| Issue | Reports | TransactionEntity |
|---|---|---|
| Zero `purchasePrice` for bonus lots | ❌ Looks like data bug | ✅ Naturally has `price=0` for bonus txn, `price=1000` for original buy |
| Split-adjusted prices | ❌ Loses original basis | ✅ Original txn preserved (still 10@1000) |
| Demerger fragmentation | ❌ Single position → two reports | ✅ Single buy txn, CA tracked separately |
| XIRR cash flows | ❌ Wrong | ✅ Correct actual cash flows |
| Audit trail | ❌ No link to CA | ✅ Each txn/BUY has `corporateActions[]` array |

---

## Honest Verdict

My earlier recommendation to enhance/replace `ReportEntity` was **partially flawed** because it did not account for how CAs fragment, mutate, and obfuscate cost basis.

### What I Got Right
- `ReportEntity` is idle and not used by critical paths.
- Pre-computed trade outcomes are useful.

### What I Got Wrong
- `ReportEntity` (and any post-CA fact table) is **NOT the right source for XIRR**.
- `ReportEntity`'s `purchasePrice` after a stock split is semantically confusing (it's adjusted, not original).
- `ReportEntity` for bonus lots has `purchasePrice=0`, which looks like a bug without a CA explanation.
- Demerger splits a single position into multiple reports, making per-stock analytics misleading.

---

## Revised Recommendation

### 1. Delete `reports` collection

It is not suitable as an analytics foundation.

### 2. XIRR must use `TransactionEntity`

Only `TransactionEntity` preserves actual cash flows:
- Original BUY: `price=1000, quantity=10` → cash outflow = 10,000
- Bonus BUY: `price=0, quantity=10` → cash outflow = 0 (correct!)
- SELL: `price=1500, quantity=20` → cash inflow = 30,000 (minus charges)

The `TransactionEntity.corporateActions[]` array also provides context for why a bonus exists.

### 3. Performance metrics should compute on-demand from `TransactionEntity + AssetEntity`

Instead of pre-storing reports, extract the FIFO matching logic from `PortfolioService.sellStock()` into a reusable `TradeMatcher` utility. For analytics:

```java
// Reconstruct FIFO-matched trades for a user's sells
List<MatchedTrade> trades = tradeMatcher.reconstructTrades(email, stockCode);

// Each MatchedTrade contains:
//   - sourceBuyTxn (original TransactionEntity)
//   - sellTxn (TransactionEntity)
//   - matchedQuantity
//   - actualCostBasis (from sourceBuyTxn)
//   - caAdjustments (list of CAs applied)
```

This gives us:
- Original cost basis (from TransactionEntity)
- CA adjustments (from AssetEntity or TransactionEntity.corporateActions)
- Zero-cost lots (bonus) are naturally handled
- Split-adjusted prices are derived, not stored

### 4. Alternative: Create a new `TradeOutcomeEntity` with dual cost basis

If the user wants pre-computed trade outcomes, design it like this:

```java
@Document("trade_outcomes")
class TradeOutcomeEntity {
    // Identity
    String email, stockCode, stockName;
    
    // Buy side (ACTUAL / post-CA dual tracking)
    double originalBuyPrice;         // Price from source TransactionEntity
    double caAdjustedBuyPrice;       // Price from AssetEntity at sell time
    double buyQuantity;
    LocalDate buyDate;
    double buyBrokerCharges, buyMiscCharges;
    
    // Sell side
    double sellPrice;
    double sellQuantity;
    LocalDate sellDate;
    double sellBrokerCharges, sellMiscCharges;
    
    // Derived
    double netProfit;                // based on caAdjustedBuyPrice (P&L)
    double originalCostBasisProfit;  // based on originalBuyPrice (for XIRR)
    int holdingPeriodDays;
    String capitalGainsType;
    String financialYear;
    
    // CA tracking
    boolean isCaDerived;             // true if this lot came from BONUS/DEMERGER/SPLIT
    List<CorporateActionType> appliedCorporateActions;
    String sourceBuyTransactionId;
    String sourceSellTransactionId;
}
```

**Pros of dual tracking:**
- `originalBuyPrice` is preserved → good for XIRR and audit
- `caAdjustedBuyPrice` is used for P&L → matches existing P&L logic
- `isCaDerived` flag explains zero-price lots to analytics consumers

**Cons:**
- More complex write path
- Need to store additional fields at sell time

### The User's Choice

| Approach | Effort | Accuracy | Historical Data |
|---|---|---|---|
| **A. Compute on-demand from TransactionEntity** | High (extract FIFO utility) | ✅ Perfect | All history available |
| **B. Create `TradeOutcomeEntity` with dual cost basis** | Medium | ✅ Perfect for XIRR + P&L | New data only (old reports deleted) |
| **C. Create `TradeOutcomeEntity` with single CA-adjusted basis** | Low-Medium | ⚠️ XIRR still needs TransactionEntity | New data only |

**My recommendation now:**

**Option B** — Create a clean `TradeOutcomeEntity` with dual cost basis (`originalBuyPrice + caAdjustedBuyPrice`). This:
- Is purpose-built for analytics
- Handles CAs correctly (explains zero-price bonus lots)
- Can serve both P&L and XIRR (with a dual-basis approach)
- Is populated at sell time (same hook as reports)
- Old reports are deleted (clean slate)

**For XIRR specifically:** Even with dual-basis, XIRR should probably still use `TransactionEntity` directly because it needs actual cash flow dates and amounts. The `TradeOutcome` can be used for trade-level metrics (win/loss, holding period), and `TransactionEntity` for XIRR.
