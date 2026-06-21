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

## Module Layout (post-Modulith refactor)

The application is organized into explicit Spring Modulith modules under `com.thiru.wealthlens`.
All modules are declared as **OPEN** (`@ApplicationModule(type = Type.OPEN)`) to expose internal types to sibling modules during the transition from a flat package layout.

| Module | Base Package | Contains | Allowed Dependencies |
|--------|-------------|----------|---------------------|
| **shared** | `com.thiru.wealthlens.shared` | Cross-cutting utilities (TCollectionUtil, ExcelBuilder/ExcelParser, XirrCalculator), common DTOs (ApiResponse, ErrorResponse, UserMail), audit entities, shared config (MongoConfig, RabbitMQ), exception handling, query helpers | `portfolio` |
| **auth** | `com.thiru.wealthlens.auth` | JWT-based Spring Security (AuthService, AuthFilter, SecurityConfig, UserDetails*), login/registration DTOs | `shared` |
| **portfolio** | `com.thiru.wealthlens.portfolio` | Core portfolio engine (PortfolioService, TransactionService, ProfitAndLossService, TradeMatchingService, AssetManagementService, AnalyticsService, TemporaryTransactionService), controllers, entities (AssetEntity, TransactionEntity, ProfitAndLossEntity), DTOs, repository interfaces, Excel export processors, migrations, report models | `shared`, `auth`, `corporate`, `brokercharges`, `helper` |
| **corporate** | `com.thiru.wealthlens.corporate` | Corporate action processing (CorporateActionService, CorporateActionController, CorporateActionEntity, LastlyPerformedCorporateAction), DTOs and enums (CorporateActionType) | `shared`, `portfolio` |
| **brokercharges** | `com.thiru.wealthlens.brokercharges` | Broker charge calculation (BrokerChargeService, UserBrokerChargeService, BrokerChargesController), entities (BrokerCharges, UserBrokerCharges), DTOs and enums (BrokerChargeTransactionType, BrokerageAggregatorType, AmcChargeFrequency) | `shared`, `portfolio`, `corporate` |
| **insurance** | `com.thiru.wealthlens.insurance` | Insurance tracking (InsuranceEntity, InsuranceService, InsuranceController, PolicyDetails), insurance DTOs and enums | `shared` |
| **finance** | `com.thiru.wealthlens.finance` | Financial calculators and controllers (FinancesController, FinancesService, StepUpSIPCalculator), finance DTOs and enums | `shared` |
| **helper** | `com.thiru.wealthlens.helper` | Auxiliary controllers (HelperController, SpecialController, TemplateController, TestController), file utilities (FileHelper, FileStream, FileType), ProfitLossDto | `shared`, `portfolio` |
| **taxplanning** | `com.thiru.wealthlens.taxplanning` | Tax-planning stub (TaxPlanningModulePlaceholder) — placeholder for future tax-planning features | `shared`, `auth` |

### Dependency Graph Summary

```
auth ──► shared ◄────── portfolio ◄───── corporate
           ▲                ▲                ▲
           │                │                │
finance ───┘         brokercharges ◄────────┘
insurance ───┘            ▲
helper ──────┘        taxplanning ◄── auth
```

- `portfolio` sits at the center of the domain and is allowed to depend on `shared`, `auth`, `corporate`, `brokercharges`, and `helper`.
- `shared` contains cross-cutting utilities and depends on `portfolio` for domain-specific DTOs/entities consumed by generic tools (XirrCalculator, ExcelBuilder, etc.).
- `brokercharges` and `corporate` are secondary domains that both depend on `portfolio` for holding/asset context.
- `auth` is the authentication/security layer and depends only on `shared`.
- `insurance`, `finance`, `taxplanning` are independent leaf modules.
- `helper` is a catch-all utility module that depends on `shared` and `portfolio`.

### Architecture Enforcement

- `ApplicationModules.verify()` runs via `WealthLensModulithTest.modulithStructureIsValid()`.
- Any cross-module usage must be declared in the consuming module's `@ApplicationModule(allowedDependencies = …)`.
- Modules expose all public types because every module is `OPEN`. Future refactors can tighten boundaries by deleting `type = Type.OPEN` and moving shared API types into the module root packages.

