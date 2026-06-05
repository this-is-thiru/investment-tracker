# Investment Tracker — Safety & Refactor Plan

## Context

- **MongoDB**: Atlas cloud-hosted (replica set), so `@Transactional` **can** enforce multi-document ACID when a `MongoTransactionManager` bean is present. However, `MongoTemplateService` wrongly has class-level `@Transactional`, and several dangerous bulk methods lack compensating safety logic.
- **Current risk**: `redriveTemporaryTransactions()` processes and deletes temp records inside a single loop. One failure mid-loop causes irrecoverable partial state (some processed, some not, temp records gone).
- **Tech stack**: Spring Boot 4.0.0, Java 25, MongoDB, Spring Data MongoDB, Lombok, Apache POI.

## Goal

1. Fix `redriveTemporaryTransactions()` to be safe, idempotent, and observable.
2. Audit all `@Transactional` usage and make transaction safety explicit.
3. Add idempotency keys (`sourceTempTransactionId`) so retries are harmless.
4. Refactor `TemporaryTransactionEntity` (extends `TransactionEntity`) → `TransactionEntity` with a `TransactionStatus` enum.

---

## Chunk 1 — Safe Redrive + RedriveResult DTO

**Files:**
- `src/main/java/com/thiru/investment_tracker/dto/RedriveResult.java`  *(new)*
- `src/main/java/com/thiru/investment_tracker/service/PortfolioService.java`  *(modify)*
- `src/main/java/com/thiru/investment_tracker/controller/TemporaryTransactionsController.java`  *(modify)*
- `src/test/java/com/thiru/investment_tracker/service/PortfolioServiceTest.java`  *(new)*

**What to change:**

1. **Create `RedriveResult`** — a plain response DTO:
   ```java
   public class RedriveResult {
       private List<String> succeeded;
       private Map<String, String> failed;          // id → error message
       private List<String> stillFiltered;          // still blocked by corporate action
       private List<String> filteredOut;            // newly re-filtered during redrive
       private String message;
   }
   ```

2. **Rewrite `PortfolioService.redriveTemporaryTransactions(UserMail)`**:
   - Return `RedriveResult` instead of `String`.
   - Iterate with **per-item try/catch**.
   - If `temporaryTransactionService.filterOutTransaction(...)` returns `true`, add to `stillFiltered` and `continue`.
   - Inside try/catch, create a fresh `List<String> itemFiltered = new ArrayList<>()`, then call `addTransaction(userMail, assetRequest, itemFiltered)`.
   - **After** `addTransaction` returns, inspect `itemFiltered`:
     - If non-empty, the transaction was re-filtered (a NEW temp record was created by `TemporaryTransactionService`). Add the **original** temp ID to `filteredOut` and **do not delete** the original temp record (it is still the source of truth).
     - If empty, the transaction was successfully processed. Add the temp ID to `succeeded`.
   - On any exception, catch, log, put `(id, message)` into `failed`, and continue.
   - **After the loop**, call `temporaryTransactionRepository.deleteAllById(succeeded)` in a single batch.
   - Set the `message` field concisely.

3. **Update `TemporaryTransactionsController`** — change `POST /redrive` to return `RedriveResult`.

4. **Tests** — `PortfolioServiceTest`:
   - Mock `temporaryTransactionRepository`, `temporaryTransactionService`, `transactionService`, `portfolioRepository`, `reportService`, `profitAndLossService`.
   - Test case: all succeed → `succeeded` list populated, `deleteAllById` called once.
   - Test case: one fails mid-loop → `succeeded` contains first items, `failed` contains bad item, `deleteAllById` only gets successes.
   - Test case: all still filtered → `stillFiltered` populated, no delete called.

**Acceptance criteria:**
- `PortfolioServiceTest` passes with 100% line coverage of `redriveTemporaryTransactions()`.
- Controller compiles and returns `RedriveResult` JSON.
- Existing integration behavior preserved for happy path.

---

## Chunk 2 — @Transactional Audit & MongoDB Transaction Safety

**Files:**
- `src/main/java/com/thiru/investment_tracker/config/MongoDbTransactionConfig.java`  *(new)*
- `src/main/java/com/thiru/investment_tracker/service/PortfolioService.java`  *(modify annotations + add safety checks)*
- `src/main/java/com/thiru/investment_tracker/service/TransactionService.java`  *(modify)*
- `src/main/java/com/thiru/investment_tracker/service/CorporateActionService.java`  *(modify)*
- `src/main/java/com/thiru/investment_tracker/service/ProfitAndLossService.java`  *(modify)*
- `src/main/java/com/thiru/investment_tracker/service/TemporaryTransactionService.java`  *(modify)*
- `src/main/java/com/thiru/investment_tracker/service/MongoTemplateService.java`  *(modify)*

**What to change:**

1. **Create `MongoDbTransactionConfig`**:
   ```java
   @Configuration
   @ConditionalOnProperty(prefix = "app.mongodb", name = "transactions-enabled", havingValue = "true")
   public class MongoDbTransactionConfig {
       @Bean
       MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
           return new MongoTransactionManager(dbFactory);
       }
   }
   ```
   Add `app.mongodb.transactions-enabled=false` to `application.yaml`.

2. **Add `TransactionSafetyUtil` *(new utility)* with one static method**:
   ```java
   public static void warnIfNoTransactions(MongoTemplate template) {
       // log at WARN once per class if replica set is not detected
   }
   ```
   This is a lightweight runtime guard.

3. **Audit and adjust `@Transactional` annotations:**
   - `PortfolioService.addTransaction` — keep `@Transactional`, add a code comment: `// Multi-document; safe only when MongoDB replica set + app.mongodb.transactions-enabled=true`.
   - `PortfolioService.uploadTransactions` — same comment.
   - `PortfolioService.redriveTemporaryTransactions` — keep `@Transactional`, but the per-item safety logic in Chunk 1 makes this annotation a "nice to have" rather than a hard dependency.
   - `PortfolioService.clearAllRecordsForCustomer` — **this is the most dangerous**. It deletes from 5 collections. Without working transactions, a crash after deleting portfolio but before deleting transactions leaves orphaned data. Add a compensating comment and consider making it iterative with try/catch.
   - `TransactionService` class-level `@Transactional` — fine, but add class Javadoc warning.
   - `CorporateActionService` — `performPendingCorporateActions` does many writes. Same treatment.
   - `MongoTemplateService` class-level `@Transactional` — this is misleading. `MongoTemplate.find()` is read-only; class-level `@Transactional` on a read service suggests false safety. **Remove it** from `MongoTemplateService`.

**Acceptance criteria:**
- Application starts cleanly with `app.mongodb.transactions-enabled=false`.
- `MongoDbTransactionConfig` is **not** instantiated when property is false.
- `MongoTemplateService` no longer has class-level `@Transactional`.
- At least one WARN-level log appears on service startup when transactions are disabled (lightweight check in a `@PostConstruct` or via `CommandLineRunner`).

---

## Chunk 3 — Idempotency Keys

**Files:**
- `src/main/java/com/thiru/investment_tracker/entity/TransactionEntity.java`  *(modify)*
- `src/main/java/com/thiru/investment_tracker/dto/AssetRequest.java`  *(modify)*
- `src/main/java/com/thiru/investment_tracker/service/TransactionService.java`  *(modify)*
- `src/main/java/com/thiru/investment_tracker/dto/TransactionResponse.java`  *(modify)*
- `src/main/java/com/thiru/investment_tracker/repository/TransactionRepository.java`  *(modify)*
- `src/test/java/com/thiru/investment_tracker/service/TransactionServiceTest.java`  *(new)*

**What to change:**

1. **Add `sourceTempTransactionId` to `TransactionEntity`**:
   ```java
   @Indexed(unique = true, sparse = true)
   @Field("source_temp_transaction_id")
   private String sourceTempTransactionId;
   ```

2. **Modify `TransactionService.addTransaction`**:
   ```java
   public String addTransaction(UserMail userMail, AssetRequest assetRequest) {
       if (assetRequest.getSourceTempTransactionId() != null) {
           Optional<TransactionEntity> existing = transactionRepository
               .findByEmailAndSourceTempTransactionId(userMail.getEmail(), assetRequest.getSourceTempTransactionId());
           if (existing.isPresent()) {
               log.warn("Duplicate transaction suppressed for tempId {}", assetRequest.getSourceTempTransactionId());
               return existing.get().getId();
           }
       }
       // ... rest of existing logic ...
   }
   ```

4. **Wire `sourceTempTransactionId`**:
   - In `AssetRequest.asTransaction()`, map `tempTransactionId` → `sourceTempTransactionId`.
   - In `AssetRequest.asAsset()`, do **not** map it (AssetEntity doesn't need it).
   - In `PortfolioService.redriveTemporaryTransactions`, pass `tempTransaction.setTempTransactionId(temporaryTransaction.getId())` so the idempotency key is set.

5. **Repository additions:**
   ```java
   Optional<TransactionEntity> findByEmailAndSourceTempTransactionId(String email, String sourceTempTransactionId);
   ```

6. **Tests:**
   - Duplicate temp ID → returns existing ID, no new save.
   - New temp ID → saves normally.

**Acceptance criteria:**
- `TransactionServiceTest` passes.
- MongoDB sparse unique index on `source_temp_transaction_id` works (unique when non-null, ignored when null).
- `PortfolioService` redrive logic passes the temp ID through so retries are safe.

---

## Chunk 4 — Merge `TemporaryTransactionEntity` into `TransactionEntity`

**Files:**
- `src/main/java/com/thiru/investment_tracker/dto/enums/TransactionStatus.java`  *(new)*
- `src/main/java/com/thiru/investment_tracker/entity/TransactionEntity.java`  *(modify)*
- `src/main/java/com/thiru/investment_tracker/entity/TemporaryTransactionEntity.java`  *(delete)*
- `src/main/java/com/thiru/investment_tracker/repository/TemporaryTransactionRepository.java`  *(delete)*
- `src/main/java/com/thiru/investment_tracker/repository/TransactionRepository.java`  *(modify)*
- `src/main/java/com/thiru/investment_tracker/service/TemporaryTransactionService.java`  *(rewrite)*
- `src/main/java/com/thiru/investment_tracker/service/PortfolioService.java`  *(modify temp references)*
- `src/main/java/com/thiru/investment_tracker/controller/TemporaryTransactionsController.java`  *(modify)*
- `src/test/java/com/thiru/investment_tracker/service/TemporaryTransactionServiceTest.java`  *(new)*
- `src/main/java/com/thiru/investment_tracker/config/TempTransactionMigrationRunner.java`  *(new)*

**What to change:**

1. **Create `TransactionStatus` enum:**
   ```java
   public enum TransactionStatus {
       TEMPORARY,
       PROCESSED,
       FAILED
   }
   ```

2. **Enhance `TransactionEntity`**:
   - Add `@Field("status") TransactionStatus status;` — default `null` means "normal processed transaction".
   - Add `@Field("asset_request") AssetRequest assetRequest;` — only populated when `status == TEMPORARY`.
   - TemporaryTransactionEntity fields that were additions (`assetRequest`) now live here.

3. **Delete `TemporaryTransactionEntity`** and its repository.

4. **Update `TransactionRepository`**:
   ```java
   List<TransactionEntity> findByEmailAndStatus(String email, TransactionStatus status);
   List<TransactionEntity> findByEmailAndStatusAndStockCodeAndAssetTypeAndTransactionDateBefore(
       String email, TransactionStatus status, String stockCode, AssetType assetType, LocalDate date);
   // ... etc
   ```

5. **Rewrite `TemporaryTransactionService`**:
   - Autowire `TransactionRepository` instead of `TemporaryTransactionRepository`.
   - `filterOutTransaction` now creates a `TransactionEntity` with `status = TransactionStatus.TEMPORARY` and `assetRequest = request`.
   - `getAllTemporaryTransactions` → `transactionRepository.findByEmailAndStatus(email, TEMPORARY)`.
   - `deleteTemporaryTransaction` → delete by status or use a bulk delete query.

6. **Update `PortfolioService`**:
   - Replace `temporaryTransactionRepository` references with `transactionRepository`.
   - `redriveTemporaryTransactions` now queries `transactionRepository.findByEmailAndStatus(email, TEMPORARY)`.
   - On success, set `status = PROCESSED` (or delete) on the temp transaction.
   - Remove `@Field` references to `temporary_transactions` collection.

7. **Update controller** — return `List<TransactionEntity>` for `getAllTemporaryTransactions`, but with status filter applied.

**Data migration:**
- Create `TempTransactionMigrationRunner` (`@Component` implementing `CommandLineRunner`):
  ```java
  @Component
  @RequiredArgsConstructor
  public class TempTransactionMigrationRunner implements CommandLineRunner {
      private final MongoTemplate mongoTemplate;

      @Override
      public void run(String... args) {
          // Query 'temporary_transactions' collection directly via MongoTemplate
          List<Document> oldDocs = mongoTemplate.find(new Query(), Document.class, "temporary_transactions");
          if (oldDocs.isEmpty()) return;

          for (Document doc : oldDocs) {
              doc.put("status", "TEMPORARY");
              doc.remove("_class"); // remove old class discriminator if present
              mongoTemplate.save(doc, "transactions");
          }
          mongoTemplate.dropCollection("temporary_transactions");
          log.info("Migrated {} temporary transactions and dropped old collection", oldDocs.size());
      }
  }
  ```
  This runs automatically on startup, so the `TemporaryTransactionRepository` deletion is safe.

**Acceptance criteria:**
- `TemporaryTransactionEntity.java` is deleted.
- `TemporaryTransactionRepository.java` is deleted.
- Application compiles; all `@Field` annotations point to `transactions`.
- `TemporaryTransactionServiceTest` passes with mocked `TransactionRepository`.
- `PortfolioServiceTest` from Chunk 1 still passes (updated mocks).

---

## Execution Order

| Order | Chunk | Why sequential |
|-------|-------|----------------|
| 1 | Chunk 1 (Safe redrive) | Independent; creates `RedriveResult` contract. |
| 2 | Chunk 2 (@Transactional audit) | Independent; safe to run after Chunk 1. |
| 3 | Chunk 3 (Idempotency) | Independent of Chunk 4; touches `TransactionEntity` minimally. |
| 4 | Chunk 4 (Temp merge) | Depends on Chunk 3 being stable (uses same `TransactionEntity`). Can run after Chunk 1-3. |

**Parallelizable:** Chunks 1, 2, and 3 can run in parallel because they don't touch the same lines. Chunk 4 must run after Chunk 3.

---

## Test Strategy

- **Unit tests** for every service change ( Mockito + JUnit 5).
- **No integration tests required** — the user runs locally and we don't have Testcontainers wired.
- **Compile check** (`./mvnw clean compile`) after every chunk.
- **Final check** (`./mvnw test`) after all chunks merged.

---

## Edge Cases

- MongoDB standalone crash during `clearAllRecordsForCustomer`: comment warns, no full fix in scope.
- `addTransaction` called with `sourceTempTransactionId` already present: duplicate suppressed, existing ID returned.
- Empty temp-transaction list on redrive: return `RedriveResult` with empty lists, not NPE.
- `deleteAllById` with empty list: MongoRepository handles this safely (no-op).
