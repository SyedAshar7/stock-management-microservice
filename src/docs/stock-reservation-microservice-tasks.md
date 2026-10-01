# Task: Build a Warehouse Stock Reservation Microservice

## Scenario

You're building a **Warehouse Stock Reservation Service** for a mid-size logistics company. Warehouse staff and order-processing systems need to check stock levels, search inventory by various filters, reserve stock when an order comes in, restock items, and deactivate discontinued SKUs. This is a single bounded-context microservice — no auth, no messaging, just clean layered REST + JPA.

---

## Database Schema (SQL Server / SSMS)

One table: `stock_item`

| Column              | Type            | Notes                                    |
|---------------------|-----------------|-------------------------------------------|
| id                  | BIGINT IDENTITY | PK                                        |
| sku                 | VARCHAR(30)     | Unique, business key                     |
| product_name        | VARCHAR(150)    | Not null                                 |
| category             | VARCHAR(50)     | e.g. ELECTRONICS, PERISHABLE, HARDWARE   |
| quantity_on_hand    | INT             | Not null, >= 0                           |
| quantity_reserved   | INT             | Not null, default 0                      |
| warehouse_location  | VARCHAR(50)     | e.g. "A-12-3"                             |
| unit_price          | DECIMAL(10,2)   | Not null                                 |
| package_length_cm   | DECIMAL(6,2)    | Part of an embedded value object          |
| package_width_cm    | DECIMAL(6,2)    | Part of an embedded value object          |
| package_height_cm   | DECIMAL(6,2)    | Part of an embedded value object          |
| last_restocked_date | DATE            | Nullable                                 |
| active              | BIT             | Default 1                                |

Note: the three `package_*` columns map to a single **embeddable** `PackageDimensions` value object (length, width, height) — used later for shelf-space planning. This is your `@Embedded` use case, so you don't need a second table for it.

---

## API Surface (5 endpoints)

| # | Method | Path                          | Purpose                                                                 |
|---|--------|-------------------------------|--------------------------------------------------------------------------|
| 1 | POST   | `/api/stock-items`             | Onboard a new SKU into the warehouse                                    |
| 2 | GET    | `/api/stock-items/{sku}`       | Fetch a single stock item by SKU                                        |
| 3 | GET    | `/api/stock-items`             | Dynamic search/filter (category, location, price range, active, min qty available) |
| 4 | PUT    | `/api/stock-items/{sku}/reserve` | Reserve a quantity against an order (decrements available, increments reserved) |
| 5 | PATCH  | `/api/stock-items/{sku}`       | Partial update — restock quantity and/or update price/location           |

(A DELETE-style "deactivate" is folded into task 5 via the PATCH endpoint setting `active=false`, to keep the API surface tight — feel free to split it into its own DELETE if you'd rather have 6.)

---

## Starter Context — Spring Initializr Setup

Generate the project with these dependencies:
- **Spring Web**
- **Spring Data JPA**
- **MS SQL Server Driver**
- **Validation** (spring-boot-starter-validation)
- **Lombok** (optional, reduces boilerplate on entities/DTOs)

Maven, Java 17+, packaging Jar.

### Suggested build order (do this before starting Task 1)
1. `application.properties` / `.yml` — SQL Server connection string, dialect, `ddl-auto`.
2. Package structure: `entity`, `embeddable`, `repository`, `dto`, `service`, `controller`, `exception`, `specification`.
3. Then follow the tasks below in order — each one assumes the previous is done.

---

## Task 1 — Domain Model & Persistence Layer

Set up the foundation the rest of the service will sit on.

- Create the `StockItem` entity mapped to the `stock_item` table. Use `@Entity` and `@Table` to bind it explicitly to the table name, and `@Column` on fields where the DB column name differs from the field name or needs constraints (nullable, unique, length).
- Create a separate `PackageDimensions` class as a JPA embeddable, and wire it into `StockItem` using `@Embedded`. Use `@Column`(or `@AttributeOverride` if you want renamed columns) inside the embeddable so the three dimension fields map correctly.
- Create the `StockItemRepository` extending `JpaRepository`. Also have it extend `JpaSpecificationExecutor` — you won't use it yet, but Task 3 depends on it being there.
- No controllers or services yet — this task is purely about getting the entity/table/repository correctly wired and the app booting against SQL Server with the table auto-generated or scripted.

**Concepts touched:** `@Entity`, `@Table`, `@Column`, `@Embedded`, repository layer setup.

---

## Task 2 — Core CRUD: Onboard & Fetch a Stock Item

Build the vertical slice for creating and reading a single item, establishing your layering pattern.

- Create a request DTO for onboarding a new SKU, and a response DTO for returning stock item data to clients. Use Jackson annotations to control the JSON shape — e.g. rename a field for the API contract, exclude an internal field from serialization, and format the date field consistently.
- Create a `StockItemService` (interface + implementation) that the controller will depend on. Wire it into the controller via constructor-based Dependency Injection — no field injection.
- Create `StockItemController` annotated with `@RestController`. Implement:
  - The **create** endpoint using `@PostMapping`, accepting the request DTO, returning the created resource wrapped in `ResponseEntity` with a `201 Created` status and a `Location`-style response body.
  - The **get-by-sku** endpoint using `@GetMapping`, returning `ResponseEntity` with `200 OK` on success.
- At this stage, if the SKU isn't found, a plain `404` via `ResponseEntity` is fine — proper exception-based handling comes in Task 5.

**Concepts touched:** Dependency Injection, `@RestController`, `@GetMapping`/`@PostMapping`, `ResponseEntity`, Jackson annotations.

---

## Task 3 — Dynamic Inventory Search

Give warehouse staff a flexible way to query inventory without writing a combinatorial explosion of repository methods.

- Create a `StockItemSpecification` (or a small set of static specification-building methods) using the **Criteria API via Spring Data Specifications** — one specification builder per filter: category equals, warehouse location equals, active equals, unit price between min/max, and quantity-on-hand greater-than-or-equal (to support "show me what's actually available").
- Combine specifications conditionally (only apply a filter if the corresponding query param was actually supplied) using `Specification.where(...).and(...)`.
- Add the **search** endpoint (`@GetMapping` on the collection path) accepting all filters as optional query parameters, delegating to the service, which calls `findAll(spec)` on the repository via `JpaSpecificationExecutor`.
- Return a `ResponseEntity` wrapping a list of the response DTO from Task 2 — reuse it, don't create a new one.

**Concepts touched:** Criteria API via Specifications, `@GetMapping`, `ResponseEntity`, DI (specifications injected/composed in the service).

---

## Task 4 — Reserve Stock (Validated Business Action)

Model the core business action of the service: reserving inventory against an incoming order.

- Create a request DTO for the reserve action (just the quantity to reserve). Apply **Bean Validation annotations** on it — quantity must be present and must be a positive number.
- In the service layer, implement the short reservation logic: look up the item by SKU, confirm `quantityOnHand - quantityReserved >= requestedQuantity`, then increment `quantityReserved` accordingly. Keep this logic intentionally simple — no state machine, no concurrency handling needed for this task.
- Create two **custom exceptions**: one for "SKU not found" and one for "insufficient available stock to reserve." These should be plain unchecked exceptions with a constructor that takes a message — don't handle them yet, that's Task 5.
- Add the **reserve** endpoint using `@PutMapping` (path includes `/reserve`), which triggers `@Valid` validation on the request DTO, calls the service, and returns the updated stock item wrapped in `ResponseEntity` with `200 OK`.

**Concepts touched:** Validation annotations, custom exceptions, `@PutMapping`, `ResponseEntity`, DI.

---

## Task 5 — Centralized Error Handling & Partial Updates

Tie the service together with consistent error responses and round out the API with restocking/deactivation.

- Create a small `ErrorResponse` DTO (timestamp, status, message, and maybe the offending field for validation errors).
- Create a single `@RestControllerAdvice` class that centralizes exception handling for the whole service:
  - Handle the two custom exceptions from Task 4, mapping "not found" to `404` and "insufficient stock" to `409 Conflict`, each returning `ResponseEntity<ErrorResponse>`.
  - Handle Bean Validation failures (from `@Valid` on any request DTO) and map them to `400 Bad Request` with a message listing what failed.
  - Add a catch-all handler for anything unexpected, mapping to `500`.
- Add the **partial update** endpoint using `@PatchMapping`, accepting a DTO where every field is optional (restock quantity, updated unit price, updated warehouse location, or setting `active=false` to discontinue the SKU). Apply only the fields that were actually supplied, save, and return the updated resource via `ResponseEntity`.
- Reuse the "SKU not found" custom exception here too, so it flows through the same advice.

**Concepts touched:** `@RestControllerAdvice`, exception handlers, custom exceptions (reused), `@PatchMapping`, `ResponseEntity`, validation.

---

## Notes on Scope

- Business logic is deliberately thin throughout (a couple of `if` checks) — the point of each task is the Spring/JPA plumbing around it, not complex domain rules.
- No JPQL or native queries anywhere — all dynamic querying goes through Specifications (Task 3).
- No repository/service tests are specified here; add them if you want extra practice, but they're not required to hit the listed concepts.
