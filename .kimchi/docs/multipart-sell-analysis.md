# Multiple Buys / Multiple Sells — Deep Analysis

> Honest admission: I understood FIFO but did not deeply trace multipart sell scenarios and their analytics implications. This document covers all combinations.

---

## How FIFO Matching Actually Works in the Code

```java
// PortfolioService.sellStock()
Iterator<AssetEntity> stockEntitiesIterator = stockEntities.iterator();
while (sellQuantity > 0) {
    AssetEntity assetEntity = stockEntitiesIterator.next();
    assetEntity.getSellTransactionIds().add(transactionId);
    Double assetQuantity = assetEntity.getQuantity();

    if (sellQuantity >= assetQuantity) {
        // Case A: Sell covers entire lot
        reportContext = toReportContext(assetEntity, assetRequest, assetQuantity);
        assetEntity.setQuantity(0D);        // lot fully consumed
        sellQuantity = sellQuantity - assetQuantity;
    } else {
        // Case B: Sell partially covers this lot
        reportContext = toReportContext(assetEntity, assetRequest, sellQuantity);
        double remainingQuantity = assetQuantity - sellQuantity;
        assetEntity.setQuantity(remainingQuantity); // lot remains
        sellQuantity = 0;
    }
    reportService.stockReport(userMail, reportContext);
    profitAndLossService.updateProfitAndLoss(userMail, profitAndLossContext);
}
```

**Key:** One SELL can create **multiple ReportEntity records** — one per matched buy lot.

---

## Scenario Traces

### Scenario 1: Multiple Buys + Single Sell

```
Buy 10 shares @ 1000 on Jan 1  → AssetEntity-1: qty=10, price=1000
Buy  5 shares @ 1200 on Feb 1  → AssetEntity-2: qty=5,  price=1200

Sell 12 shares @ 1500 on Mar 1 → transactionId = "txn-sell-1"

FIFO walk:
  Lot 1 (AssetEntity-1): qty=10, sell needs 12 → full consumption
    Report-1: purchasePrice=1000, sellPrice=1500, sellQty=10
    AssetEntity-1: qty=0 → will be deleted
    sellQuantity remaining = 2

  Lot 2 (AssetEntity-2): qty=5, sell needs 2 → partial consumption
    Report-2: purchasePrice=1200, sellPrice=1500, sellQty=2
    AssetEntity-2: qty=3 → remains in portfolio
    sellQuantity remaining = 0 → DONE

Reports created: 2
Portfolio after sell: AssetEntity-2 with qty=3
```

**Works correctly.** Two reports accurately represent the two buy lots consumed.

---

### Scenario 2: Single Buy + Multiple Sells

```
Buy 10 shares @ 1000 on Jan 1 → AssetEntity-1: qty=10, price=1000

Sell 3 shares @ 1200 on Mar 1  → "txn-sell-1"
  Lot 1: qty=10, sell needs 3 → partial
    Report-1: purchasePrice=1000, sellPrice=1200, sellQty=3
    AssetEntity-1: qty=7

Sell 4 shares @ 1500 on Apr 1  → "txn-sell-2"
  Lot 1: qty=7, sell needs 4 → partial
    Report-2: purchasePrice=1000, sellPrice=1500, sellQty=4
    AssetEntity-1: qty=3

Sell 3 shares @ 1600 on May 1  → "txn-sell-3"
  Lot 1: qty=3, sell needs 3 → full
    Report-3: purchasePrice=1000, sellPrice=1600, sellQty=3
    AssetEntity-1: qty=0 → deleted

Reports: 3 (one per sell, same lot)
```

**Also works correctly.** Each sell creates one report because all three sells hit the same buy lot.

---

### Scenario 3: Multiple Buys + Multiple Sells

```
Buy 10 shares @ 1000 on Jan 1 → AssetEntity-1: qty=10
Buy 10 shares @ 1200 on Feb 1 → AssetEntity-2: qty=10

Sell 12 shares @ 1500 on Mar 1 → "txn-sell-1"
  Lot 1: qty=10 → full, Report-1 (10 shares)
  Lot 2: qty=10, needs 2 → partial, Report-2 (2 shares)
  AssetEntity-1: deleted
  AssetEntity-2: qty=8

Sell 8 shares @ 1600 on Apr 1 → "txn-sell-2"
  Lot 2: qty=8 → full, Report-3 (8 shares)
  AssetEntity-2: deleted

Reports: 3 (2 reports for first sell, 1 report for second sell)
Total lots consumed: 3 (10+2 from first sell, 8 from second sell)
```

**Works correctly.** Three reports for two sell transactions.

---

### Scenario 4: Same-Day Buys (Merged into One AssetEntity)

```
Buy 5 shares @ 1000 on Jan 1 → AssetEntity-1 created: qty=5, price=1000
Buy 5 shares @ 1100 on Jan 1 → AssetEntity-1 UPDATED:
   newQty = 5 + 5 = 10
   newPrice = (5*1000 + 5*1100) / 10 = 1050

Sell 10 shares @ 1500 on Mar 1
  Lot 1: qty=10 → full, Report-1 (10 shares @ avg buy 1050)

Note: Individual buy prices (1000 and 1100) are LOST after merge.
Only the averaged price (1050) is stored in AssetEntity.
TransactionEntity preserves the original two buy transactions.
```

**Implication for analytics:** Same-day buys are indistinguishable in AssetEntity. TransactionEntity has the original transactions, but FIFO matching at the lot level loses granularity.

---

## What the Code Gets Wrong for Analytics

### Bug 1: Reports Cannot Be Grouped by Sell Transaction

```java
String transactionId = addTransactionInternal(userMail, assetRequest); // SELL txn id
// ... inside loop ...
reportService.stockReport(userMail, reportContext); // Report created WITHOUT transactionId
```

**Result:** If a sell of 100 shares creates 3 reports, there is **no way to know** which reports belong to the same sell transaction.

**Impact:**
- Cannot compute "per-sell-transaction P&L" (how much did this sell order make?)
- Cannot compute "sell order win rate" (did sell order #1 make money overall?)
- Analytics MUST aggregate by matched lot, not by sell order

**For TradeOutcome, this is fixed by adding `sourceSellTransactionId`.**

---

### Bug 2: Reports Do Not Track Sell-Side Charges

```java
// ProfitAndLossContext.from() — PRO-RATES charges:
double sellBrokerCharge = (assetRequest.getBrokerCharges() / assetRequest.getQuantity()) * sellQuantity;
double sellMiscCharge = (assetRequest.getMiscCharges() / assetRequest.getQuantity()) * sellQuantity;

// toReportContext() — NEVER sets charges:
reportContext.getBrokerCharges(); // stays 0 (not even set)
```

**Result:** A sell of 100 shares with Rs 500 charges, split across 3 reports:
- Report 1 (30 shares): sell charges = 0
- Report 2 (40 shares): sell charges = 0
- Report 3 (30 shares): sell charges = 0

The correct charges should be:
- Report 1: Rs 150 (30/100 * 500)
- Report 2: Rs 200 (40/100 * 500)
- Report 3: Rs 150 (30/100 * 500)

**Impact:** Net profit in reports is overstated by the full sell charges. (P&L is correct, but reports are wrong.)

---

### Bug 3: Same-Day Buy Merge Loses Granularity

If a user buys 5@1000 and 5@1100 on the same day, the AssetEntity stores `10@1050`. Any subsequent sell from that lot creates a report with `purchasePrice=1050`.

**Impact:** "Best buy day" analytics can't distinguish between the 1000 buy and the 1100 buy.

**Mitigation:** TransactionEntity preserves individual buys. For detailed per-buy analytics, we'd need to read TransactionEntity, not AssetEntity.

---

### Bug 4: Partial-Lot Sell Creates Reports Without Remaining Lot Reference

When a lot of 10 is partially sold (3 sold, 7 remain), a report is created for the 3. But there is no report or record linking the remaining 7 to its source lot for future analytics.

**Impact:** If the user later sells the remaining 7, that creates a new report. But you can't trace the lifecycle of a single buy lot through multiple partial sells without following AssetEntity's `sellTransactionIds` array.

---

## Analytics Granularity: What Can We Actually Compute?

### Level 1: Per-Matched-Lot (from Reports/TradeOutcome)

A matched lot = one AssetEntity lot consumed by (part of) a sell.

Examples from Scenario 3:
- Report 1: Lot-1 (Jan 1 buy), fully consumed by Sell-1 (Mar 1) → 10 shares, profit = (1500-1000)*10
- Report 2: Lot-2 (Feb 1 buy), partially consumed by Sell-1 (Mar 1) → 2 shares, profit = (1500-1200)*2
- Report 3: Lot-2 (Feb 1 buy), fully consumed by Sell-2 (Apr 1) → 8 shares, profit = (1600-1200)*8

**Metrics possible:**
- Which buy lot was most profitable?
- Average holding period of individual lots
- Total P&L per stock, per broker, etc.

### Level 2: Per-Sell-Transaction (requires grouping)

Examples from Scenario 3:
- Sell-1 (Mar 1): consumed 10 from Lot-1 + 2 from Lot-2
  - Total sell quantity: 12
  - Avg buy price: weighted average = (1000*10 + 1200*2) / 12 = 1033.33
  - Implied profit per share: 1500 - 1033.33 = 466.67
  - Total profit: 466.67 * 12 = 5600
  
- Sell-2 (Apr 1): consumed 8 from Lot-2
  - Total sell quantity: 8
  - Avg buy price: 1200
  - Profit per share: 1600 - 1200 = 400
  - Total profit: 400 * 8 = 3200

**Metrics possible:**
- "My sell order on Mar 1 made Rs 5,600"
- Win/loss rate per sell order
- Average profit per sell order

**To compute this:** We MUST group reports by `sourceSellTransactionId`. Currently impossible because reports lack this field.

### Level 3: Per-Stock Aggregated

Sum across all reports for a stock:
- Total shares sold: 20
- Total cost basis: 10000 + 12000 = 22000
- Total sell value: 1500*12 + 1600*8 = 18000 + 12800 = 30800
- Total profit: 30800 - 22000 = 8800

**Metrics possible:**
- Best/worst stock by total realized profit
- Total return % per stock

---

## What Analytics Each Data Source Can Support

| Metric | AssetEntity | TransactionEntity | ReportEntity (current) | TradeOutcome (proposed) |
|---|---|---|---|---|
| **Current holdings / portfolio value** | ✅ | ❌ | ❌ | ❌ |
| **XIRR (cash flows)** | ❌ | ✅ | ❌ | ⚠️ Dual-basis only |
| **Per-lot profit** | ⚠️ Partial | ❌ Needs FIFO | ✅ | ✅ |
| **Per-sell profit** | ❌ | ❌ Needs FIFO | ❌ (no txn link) | ✅ (with txnId) |
| **Per-stock total profit** | ❌ | ❌ Needs FIFO | ✅ (sum reports) | ✅ |
| **Win/loss per lot** | ❌ | ❌ Needs FIFO | ✅ | ✅ |
| **Win/loss per sell order** | ❌ | ❌ Needs FIFO | ❌ | ✅ (with txnId) |
| **Avg holding period** | ❌ | ❌ Needs FIFO | ✅ (avg of reports) | ✅ |
| **Charges per matched portion** | ❌ | ❌ | ❌ (charges not stored) | ✅ (pro-rated) |

---

## The Honest Answer to Your Question

**Did I consider multiple buys/sells?**

I understood that FIFO creates multiple reports for multipart sells, but I **did NOT deeply trace the implications** for:

1. **Per-sell-transaction metrics** (how much did this sell order make?). This is the most natural user view of a "trade" but is currently uncomputable from reports.

2. **Same-day buy merging** (where individual buy prices are lost in AssetEntity, making some lot-level profit analytics impossible).

3. **Sell-side charge pro-rating** (where reports miss charges entirely, making net profit incorrect in reports even though P&L is correct).

For analytics to be meaningful, we need to decide the **granularity**:

- **Lot-level** (FIFO-matched buy lot → sell portion): This is closest to what reports already do. Good for tax calculations (STCG/LTCG per lot).
- **Sell-transaction-level** (one sell order → all matched lots aggregated): This is what users intuitively think of as "a trade." Good for performance dashboards ("My INFY sell on March 1st made Rs 5,600"). Requires `sourceSellTransactionId`.

**My revised recommendation for TradeOutcome:**

Store at **lot-level** (one record per matched portion) with a `sourceSellTransactionId` field. Analytics can then aggregate up to sell-transaction-level when needed.
