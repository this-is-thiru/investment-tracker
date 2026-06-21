# Investment Tracker — Agent Instructions

## Project Overview

Spring Boot + MongoDB portfolio tracking application for Indian stock market investments. Manages buy/sell transactions, corporate actions (bonus, demerger, stock split), profit/loss calculation, and portfolio holdings.

## Tech Stack

- **Java 25** with Spring Boot 4.0.0
- **MongoDB** (Atlas with replica set for transactions)
- **Maven** (`./mvnw` for builds)
- **Lombok**, **Log4j2**, **Jackson**, **Spring Security + JWT**

## Architecture

```
controller/  → REST endpoints, no business logic
service/     → All business logic, @Transactional for writes
repository/  → Spring Data MongoDB, query-by-method-name
entity/      → @Document classes with snake_case field names
dto/         → Request/response DTOs with asTransaction()/asAsset() converters
util/        → Static utility classes (prefix: T — TCollectionUtil, TLocalDate, etc.)
```

## Coding Conventions

### Naming
- Classes: `PascalCase` — `TransactionService`, `AssetEntity`
- Methods/variables: `camelCase` — `addTransaction`, `stockCode`
- Enums/constants: `UPPER_SNAKE` — `BUY`, `TEMPORARY`
- Package folders: lowercase
- Test classes: `XxxServiceTest`
- Test methods: `methodName_whenCondition_expectedResult`

### Style
- 4-space indentation (IntelliJ default)
- K&R braces (`{` on same line)
- `@RequiredArgsConstructor` for DI — no explicit constructors
- Constructor-injected fields are `private final`

### Lombok
- Services: `@Service @Log4j2 @Transactional @RequiredArgsConstructor`
- Entities: `@Data @AllArgsConstructor @NoArgsConstructor @Document @Field`
- DTOs: `@Data @Getter @Setter @NoArgsConstructor @ToString`
- Avoid `@Builder` on request DTOs

### Annotations
- Class-level annotations on separate lines above class declaration
- `@RequestMapping` paths: lowercase with hyphens — `@RequestMapping("/corporate-action/")`
- `@Transactional` at class level for services doing multi-collection writes

## MongoDB Conventions

- Collection names: `snake_case` — `@Document(value = "transactions")`
- Field names: `snake_case` — `@Field("stock_code")`
- Id field: `@MongoId`
- Unique indexes: `@Indexed(unique = true, sparse = true)`
- Entities implement `AuditableEntity` for audit metadata
- Spring Data derived queries: `findByEmailAndStatus`, `deleteByEmailAndStatus`

## Error Handling

- `BadRequestException extends IllegalArgumentException` for user-facing validation
- `IllegalArgumentException` for data validation (missing fields, invalid data)
- `ControllerAdviser` maps: `IllegalArgumentException` → 400, `Exception` → 500
- Messages are human-readable strings, never error codes

## API Conventions

- Base paths: `/transactions/user/{email}`, `/portfolio/user/{email}`
- Resource paths: lowercase, hyphenated — `/upload-transactions`, `/profit-and-loss`
- `@PathVariable` for path params, `@RequestBody` for body, `@RequestParam` for query params
- Date JSON format: `yyyy-MM-dd` via `@JsonFormat`
- DateTime JSON format: `yyyy-MM-dd'T'HH:mm:ss`

## Testing

- `@ExtendWith(MockitoExtension.class)`
- `@Mock` for dependencies, `@InjectMocks` for service under test
- Structure: `// Given`, `// When`, `// Then`
- Stubbing: `when(...).thenReturn(...)`
- Verification: `verify(...).method(...)`
- Run: `./mvnw test -Dtest=XxxServiceTest`

## Domain Rules

### Transaction Flow
1. Check for existing temporary transactions → throw if any exist
2. Check if corporate action blocks the transaction → save as `TEMPORARY`
3. Otherwise process: BUY upserts `AssetEntity`, SELL validates quantity and updates P&L

### Corporate Actions
- Types: `BONUS`, `DEMERGER`, `STOCK_SPLIT`, `NAME_OR_SYMBOL_CHANGE`
- `FILTERABLE_CORPORATE_ACTIONS` (BONUS, DEMERGER, STOCK_SPLIT) block new transactions
- Use `LastlyPerformedCorporateAction` to track per-user/stock/broker processing state

### Temporary Transactions
- Stored in `transactions` collection with `status = TEMPORARY`
- Original `AssetRequest` stored in `assetRequest` field for redrive
- `sourceTempTransactionId` links processed transactions back to originals
- Must be redriven via `POST /temporary-transactions/user/{email}/redrive`

## Important Constraints

- MongoDB replica set required for `@Transactional` multi-document writes
- `app.mongodb.transactions-enabled=true` must be set for transactions
- Date converters (`LocalDateToDateConverter`, `DateToLocalDateConverter`) registered in `MongoConfig`
- All time values stored in UTC via `TLocalDateTime.atUtc()`
- Sell transactions use FIFO-like quantity allocation across purchase lots

## Key Utilities

| Utility | Purpose |
|---------|---------|
| `TCollectionUtil` | Map, filter, group, sum operations on collections |
| `TJsonMapper` | JSON serialization/deserialization |
| `TLocalDate` / `TLocalDateTime` / `TLocalTime` | Date/time formatting and conversion |
| `ExcelBuilder` / `ExcelParser` | Excel file parsing and generation |
| `QueryBuilder` / `QueryFilter` | Dynamic MongoDB query construction |
