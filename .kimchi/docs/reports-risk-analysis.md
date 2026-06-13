# Reports Collection — Risk Analysis For Schema Changes

> Do any critical features depend on `reports`? Is it safe to alter?

---

## Where Reports Are Referenced (Full Scan)

| File | Line | Reference | Critical? |
|---|---|---|---|
| `PortfolioService.java` | 425 | `reportService.stockReport(userMail, reportContext)` | **Yes — write path** |
| `PortfolioService.java` | 504 | `reportService.deleteReports(userMail)` | Yes — cleanup path |
| `PortfolioService.java` | 619 | `reportService.updateReports()` | No — one-time backfill (edge case) |
| `ReportController.java` | 21 | `reportService.getStockReport(UserMail.from(email))` | **No — read-only endpoint** |
| `AuthConfig.java` | 38 | `/reports/**` in request matchers | No — security config |
| `ReportService.java` | 23 | `reportRepository.save(reportEntity)` | Yes — write path |
| `ReportService.java` | 31 | `reportRepository.findByEmail(email)` | No — read-only |
| `ReportService.java` | 35 | `reportRepository.deleteByEmail(email)` | Yes — cleanup path |
| `ReportService.java` | 39 | `reportRepository.findAll()` | No — one-time migration |
| `ReportRepository.java` | 11 | `findByEmail(String email)` | No — read-only |

---

## What Critical Features Use Reports?

### ✅ Write Path (Sell Transaction)

```java
// PortfolioService.sellStock() → line ~425
reportService.stockReport(userMail, reportContext);
```

Every SELL transaction creates a `ReportEntity`. This is **actively running** every time a user sells a stock.

**Impact of altering ReportEntity on this path:**
- New fields → `TJsonMapper.copy()` will populate them from `ReportContext` (if added)
- No fields removed → serialization is backward-compatible
- MongoDB stores new fields as new document keys → schemaless, no migration needed

**Risk level:** **Very Low** for additive changes. High risk only if removing/changing existing field logic.

### ❌ Read Path (Report Viewer)

```java
// ReportController.java → GET /reports/user/{email}
return reportService.getStockReport(UserMail.from(email));
```

This is the **only** read consumer. It simply lists all reports for a user. No downstream business logic depends on it.

**Impact of altering ReportEntity on this path:**
- Old documents show `null` for new fields → acceptable
- `ReportController` returns the entity directly → frontend may show `null`

**Risk level:** **Very Low.**

### ❌ Profit & Loss Path

```java
// ProfitAndLossService uses ProfitAndLossContext, NOT ReportEntity
```

P&L is computed from `ProfitAndLossContext` → saved to `ProfitAndLossEntity`. **Zero dependency** on `ReportEntity`.

### ❌ Portfolio / Asset Path

```java
// PortfolioService reads AssetEntity, not ReportEntity
```

Portfolio holdings are computed from `AssetEntity`. **Zero dependency** on `ReportEntity`.

### ❌ Corporate Action Path

```java
// CorporateActionService reads AssetEntity + CorporateActionEntity
```

**Zero dependency** on `ReportEntity`.

### ❌ Clear All Data Path

```java
// PortfolioService.clearAllRecordsForCustomer()
reportService.deleteReports(userMail);
```

Calls `deleteByEmail`. This is a cleanup operation. Changes to `ReportEntity` schema have **zero impact** on deletion.

### ❌ One-Time Backfill (`updateReports`)

```java
// PortfolioService.updateTransactions() → line 619
reportService.updateReports();
```

Iterates ALL reports to backfill `assetType`. This is a **manual/one-time migration** method. Not called automatically. Not critical.

---

## Risk Assessment Matrix

| Change | Risk Level | Reason |
|---|---|---|
| **Add new fields** (e.g., `netProfit`, `holdingPeriodDays`) | 🟢 Very Low | MongoDB is schemaless; old docs stay as-is; new docs get new fields |
| **Fix `totalValue` bug** (was always 0) | 🟢 Very Low | Fixes existing issue; old docs unaffected; new docs get correct value |
| **Fix `quantity` type** (Long → Double) | 🟢 Very Low | Jackson auto-deserializes existing Long values to Double; new writes use Double |
| **Change / remove existing fields** | 🔴 High | Could break `TJsonMapper.copy()`, existing queries, or consumers |
| **Add required index** | 🟢 Very Low | `findByEmail` is the only query; already efficient |

---

## What Breaks If We Change `ReportEntity`?

### Additive Changes (adding fields)

| Scenario | Break? | Why |
|---|---|---|
| New `ReportEntity` + old MongoDB documents | ❌ No | Spring Data ignores missing fields (they stay `null`) |
| Old `ReportContext` + new `ReportEntity` | ❌ No | Extra fields in entity simply remain `null` if not set in context |
| `TJsonMapper.copy(context, entity)` with new fields | ❌ No | Jackson copies matching fields; new fields in entity are left as default (`null` / `0`) |
| `ReportController` returning old documents with new fields | ❌ No | JSON response shows `null` for missing fields — valid JSON |
| `PortfolioService.sellStock()` writing new schema | ❌ No | Just adds a few arithmetic operations at write time |

### The Only Migration Needed

If we want **existing reports** to have the new pre-computed fields, we need to either:
1. Re-process all historical sell transactions (hard — requires reconstructing context)
2. Compute new fields on-the-fly when reading old reports (compatibility mode)
3. Leave old reports as `null` and only use new fields for reports created after deployment

**Recommendation:** Option 3 — new fields start populating from deployment onward. Analytics can skip reports with `null` new fields, or compute them at read time as a fallback.

---

## Verdict

**Risk: Very Low. Safe to alter.**

- No critical business logic reads `ReportEntity` for decision-making.
- The only write path is `sellStock()` which gains a few harmless arithmetic operations.
- MongoDB's schemaless nature makes additive changes trivial.
- The only read consumer (`/reports/user/{email}`) is a passive list endpoint.
- The user confirmed reports were intentionally kept idle because `assets`, `transactions`, and `profit_and_loss` cover the critical paths.

---

## Recommended Approach

1. **Enhance `ReportEntity`** with Tier 2 analytics fields (add only, no removal)
2. **Enhance `ReportContext`** to carry new fields
3. **Update `PortfolioService.toReportContext()`** to compute and populate new fields
4. **Keep old documents** as-is; they will have `null` for new fields
5. **In `AnalyticsService`**, handle `null` fields gracefully (compute at read time as fallback, or skip old reports)

This is a safe, low-risk schema enhancement that turns an idle collection into an analytics-ready source.
