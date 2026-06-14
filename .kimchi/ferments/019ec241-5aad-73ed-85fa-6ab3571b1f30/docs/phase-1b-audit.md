# Phase 1b Audit: Transaction-Based TradeOutcome Migration

## Completed

### Files Created
1. `TradeMatchingService.java` - Extracted FIFO matching logic as a reusable service
   - `BuyLot`, `SellRequest`, `MatchedTrade` inner static classes
   - `buildLotsFromBuys()` - merges same-day buys with weighted average price
   - `matchSellToLots()` - FIFO matching with pro-rated charges
   - `toTradeOutcomeContext()` - converts MatchedTrade to TradeOutcomeContext
   - `toTradeOutcomeEntity()` - converts MatchedTrade to TradeOutcomeEntity
   - `toSellRequest()` - convenience converter from TransactionEntity

2. `TransactionBasedTradeOutcomeMigration.java` - New migration using TransactionEntity BUY/SELL records

3. `TradeMatchingServiceTest.java` - Unit tests covering:
   - Same-day buy merging with weighted average
   - Single lot sell matching
   - Multi-lot sell matching (FIFO)
   - Pro-rated charge calculations
   - Partial lot sell
   - Full lot consumption then next lot
   - CA-derived buy identification
   - TradeOutcomeEntity conversion
   - TransactionEntity to SellRequest conversion

### Files Deleted
- `TradeOutcomeMigration.java` - Old ReportEntity-based migration

### Files Modified
- None (PortfolioService left unchanged as scoped)

### Verification
- `mvn compile -pl backend -q` ✓
- `mvn test -pl backend -Dtest=TradeMatchingServiceTest` ✓ (11 tests)
- `mvn test -pl backend -Dtest=AnalyticsServiceTest` ✓

### Key Notes
- `isCaDerived` is primitive `boolean`, generates `isCaDerived()` not `getIsCaDerived()`
- `AccountType.INVESTMENT` doesn't exist - used `AccountType.SELF` instead
- PortfolioService NOT refactored (out of scope per user request)