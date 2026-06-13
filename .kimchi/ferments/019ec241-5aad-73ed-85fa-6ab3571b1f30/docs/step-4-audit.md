# Step 4 Audit: Delete ReportEntity and References

## Date: 2026-06-14

## Action: Delete ReportEntity, ReportContext, ReportService, ReportRepository, ReportController

### Files Deleted

| File | Reason |
|------|--------|
| `entity/ReportEntity.java` | Core entity — superseded by TradeOutcomeEntity |
| `dto/context/ReportContext.java` | Helper DTO used only by ReportService |
| `service/ReportService.java` | Business logic layer for reporting |
| `repository/ReportRepository.java` | Spring Data repo for `reports` collection |
| `controller/ReportController.java` | REST endpoint `/reports/user/{email}` |
| `integration/ReportIntegrationTest.java` | Integration tests for the above (5 test methods) |

### Files Updated

| File | Change |
|------|--------|
| `AuthConfig.java` | Removed `/reports/**` from authenticated request matchers; added `/analytics/**` (future-proofing for Step 5 endpoints) |
| `TradeOutcomeMigration.java` | Replaced `ReportRepository` injection with `MongoTemplate` to query the `reports` collection directly. This was necessary because `ReportRepository` itself was deleted. The migration still reads from the `reports` collection (source of truth for existing user data) but now accesses it via raw `Document` BSON, then maps to `TradeOutcomeEntity`. Enum fields (BrokerName, AssetType, AccountType) parsed with a new `parseEnum()` helper that safely handles unknown values. |

### Compilation Verification

```
./mvnw compile -pl backend -q
```
Result: **PASS** — only JVM warnings (jansi, sun.misc.Unsafe), no compile errors.

### Test Verification

```
./mvnw test -pl backend -Dtest=PortfolioServiceTest,TradeOutcomeMigrationTest -q
```
Result: **PASS** — `PortfolioServiceTest` passes (already refactored to use `TradeOutcomeService` in Step 3). `TradeOutcomeMigrationTest` passes with updated `MongoTemplate`-based dependency.

### Design Notes

- `TradeOutcomeMigration` still reads from the `reports` collection (via `MongoTemplate.findAll(Document.class, "reports")`). This preserves the one-time migration capability — legacy user data in `reports` can be migrated to `trade_outcomes` before the `reports` collection is dropped.
- The `parseEnum()` helper prevents the migration from failing hard on legacy data with unexpected enum string values; such fields are set to `null` with a WARN log.
- `TradeOutcomeMigration` no longer has any compile-time dependency on any deleted class.

### Remaining References

No remaining references to any deleted class found in `backend/src/main/` or `backend/src/test/`.

## Commit

All changes committed to `feature/advanced-analytics-v1` with commit message: `Step 4: Delete ReportEntity and all report-related classes; update TradeOutcomeMigration to use MongoTemplate`