# Warehouse Stock Reservation Microservice

A single bounded-context Spring Boot microservice designed for mid-size logistics and warehouse stock management. This service provides a clean, layered RESTful API for onboarding SKUs, fetching inventory items, executing dynamic spec-based search queries, reserving stock against incoming orders, restocking inventory, and deactivating discontinued SKUs.

---

## 🏛️ Architectural Overview & Layering

The microservice strictly enforces a **N-Tier Layered Architecture** with unidirectional data flow and strong separation of concerns.

```
                  ┌─────────────────────────────────────────┐
                  │              HTTP Client                │
                  └────────────────────┬────────────────────┘
                                       │ Request / Response (JSON)
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ CONTROLLER LAYER                                                            │
│ • StockItemController (@RestController)                                     │
│ • BaseController (@RestControllerAdvice for Global Exception Handling)       │
│ • Triggers DTO Validation (@Valid)                                          │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Transfer DTOs
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ SERVICE LAYER                                                               │
│ • StockItemService (Interface) & StockItemServiceImpl (Implementation)      │
│ • Implements domain business rules (e.g. available = onHand - reserved)    │
│ • Maps DTOs ⟷ Entities & orchestrates specifications                         │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Domain Entities (StockItem)
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ REPOSITORY & SPECIFICATION LAYER                                           │
│ • StockItemRepository (JpaRepository + JpaSpecificationExecutor)           │
│ • StockItemSpecification (JPA Criteria API builder)                        │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ SQL Queries / JDBC
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ DATABASE                                                                    │
│ • MS SQL Server / Relational Database (`stock_item` table)                  │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 1. Controller Layer ([`controller/`](file:///d:/Learn/Spring%20Boot/Exercises/web/microservice-app/stock-reservation-service/src/main/java/com/capacity/stock_reservation_service/controller/))
* **[`StockItemController.java`](file:///d:/Learn/Spring%20Boot/Exercises/web/microservice-app/stock-reservation-service/src/main/java/com/capacity/stock_reservation_service/controller/StockItemController.java):** Exposes RESTful HTTP endpoints (`/api/stock-items`). Delegates execution directly to the service layer and returns wrapped standard response envelopes (`ApiResponse<T>`).
* **[`BaseController.java`](file:///d:/Learn/Spring%20Boot/Exercises/web/microservice-app/stock-reservation-service/src/main/java/com/capacity/stock_reservation_service/controller/BaseController.java):** Annotated with `@RestControllerAdvice`. Intercepts service-level exceptions (`DuplicateSkuException`, `StockItemNotFoundException`, `InsufficientStockException`, `MethodArgumentNotValidException`) and maps them to HTTP status codes (`400`, `404`, `409`, `500`).

### 2. Service Layer ([`service/`](file:///d:/Learn/Spring%20Boot/Exercises/web/microservice-app/stock-reservation-service/src/main/java/com/capacity/stock_reservation_service/service/))
* **[`StockItemService.java`](file:///d:/Learn/Spring%20Boot/Exercises/web/microservice-app/stock-reservation-service/src/main/java/com/capacity/stock_reservation_service/service/StockItemService.java) & [`StockItemServiceImpl.java`](file:///d:/Learn/Spring%20Boot/Exercises/web/microservice-app/stock-reservation-service/src/main/java/com/capacity/stock_reservation_service/service/StockItemServiceImpl.java):** Houses core business logic. Enforces SKU uniqueness, computes available quantities (`quantityOnHand - quantityReserved`), validates reservation limits, manages partial updates, and handles mapping between DTOs and persistent JPA entities.

### 3. Repository & Specification Layer ([`repository/`](file:///d:/Learn/Spring%20Boot/Exercises/web/microservice-app/stock-reservation-service/src/main/java/com/capacity/stock_reservation_service/repository/), [`specification/`](file:///d:/Learn/Spring%20Boot/Exercises/web/microservice-app/stock-reservation-service/src/main/java/com/capacity/stock_reservation_service/specification/))
* **[`StockItemRepository.java`](file:///d:/Learn/Spring%20Boot/Exercises/web/microservice-app/stock-reservation-service/src/main/java/com/capacity/stock_reservation_service/repository/StockItemRepository.java):** Spring Data JPA interface extending both `JpaRepository` and `JpaSpecificationExecutor`.
* **[`StockItemSpecification.java`](file:///d:/Learn/Spring%20Boot/Exercises/web/microservice-app/stock-reservation-service/src/main/java/com/capacity/stock_reservation_service/specification/StockItemSpecification.java):** Employs the **JPA Criteria API** to build type-safe, programmatic search specifications.

### 4. Domain & Embeddable Model ([`model/`](file:///d:/Learn/Spring%20Boot/Exercises/web/microservice-app/stock-reservation-service/src/main/java/com/capacity/stock_reservation_service/model/), [`embeddable/`](file:///d:/Learn/Spring%20Boot/Exercises/web/microservice-app/stock-reservation-service/src/main/java/com/capacity/stock_reservation_service/embeddable/))
* **[`StockItem.java`](file:///d:/Learn/Spring%20Boot/Exercises/web/microservice-app/stock-reservation-service/src/main/java/com/capacity/stock_reservation_service/model/StockItem.java):** The JPA `@Entity` mapped to `stock_item`.
* **[`PackageDimensions.java`](file:///d:/Learn/Spring%20Boot/Exercises/web/microservice-app/stock-reservation-service/src/main/java/com/capacity/stock_reservation_service/embeddable/PackageDimensions.java):** A reusable `@Embeddable` Value Object embedded into `StockItem` via `@Embedded` and mapped column attributes (`lengthCm`, `widthCm`, `heightCm`).

---

## 🔄 Layer Communication Protocol: DTOs vs. Entities

To prevent tight coupling between DB schema models and API clients, domain entities are strictly confined to the persistence and service layers. External callers interact exclusively through **Data Transfer Objects (DTOs)**.

```
[ HTTP Payload (JSON) ]
          │
          ▼
┌──────────────────┐
│   Inbound DTO    │  e.g. CreateStockItemRequest, ReserveStockRequest, StockItemPatchRequest
└────────┬─────────┘  (Validated via @Valid & Jakarta Validation)
         │
         │ Service Layer Conversion (createRequestToEntity)
         ▼
┌──────────────────┐
│   JPA Entity     │  StockItem + PackageDimensions (@Embedded)
└────────┬─────────┘  (Saved to Database)
         │
         │ Service Layer Conversion (toResponse)
         ▼
┌──────────────────┐
│   Outbound DTO   │  StockItemResponse
└────────┬─────────┘  (Renamed fields via Jackson @JsonProperty)
         │
         ▼
[ ApiResponse<T> Generic Envelope ]
```

### 1. Inbound Request DTOs ([`dto/`](file:///d:/Learn/Spring%20Boot/Exercises/web/microservice-app/stock-reservation-service/src/main/java/com/capacity/stock_reservation_service/dto/))
- **[`CreateStockItemRequest.java`](file:///d:/Learn/Spring%20Boot/Exercises/web/microservice-app/stock-reservation-service/src/main/java/com/capacity/stock_reservation_service/dto/CreateStockItemRequest.java):** Enforces mandatory creation contracts using annotations like `@NotEmpty`, `@NotNull`, `@Positive`, and `@PositiveOrZero`.
- **[`ReserveStockRequest.java`](file:///d:/Learn/Spring%20Boot/Exercises/web/microservice-app/stock-reservation-service/src/main/java/com/capacity/stock_reservation_service/dto/ReserveStockRequest.java):** Captures reservation requests with `@Positive` quantity validation.
- **[`StockItemPatchRequest.java`](file:///d:/Learn/Spring%20Boot/Exercises/web/microservice-app/stock-reservation-service/src/main/java/com/capacity/stock_reservation_service/dto/StockItemPatchRequest.java):** Represents partial field updates (restocking, price change, location update, SKU deactivation).

### 2. Outbound Response DTO
- **[`StockItemResponse.java`](file:///d:/Learn/Spring%20Boot/Exercises/web/microservice-app/stock-reservation-service/src/main/java/com/capacity/stock_reservation_service/dto/StockItemResponse.java):** Decouples internal database column names from external JSON contracts. Computed fields like `quantity_available` (`onHand - reserved`) are calculated dynamically before serialization.

### 3. Standard Unified API Envelope
- **[`ApiResponse<T>`](file:///d:/Learn/Spring%20Boot/Exercises/web/microservice-app/stock-reservation-service/src/main/java/com/capacity/stock_reservation_service/dto/ApiResponse.java):** Wraps all success and failure HTTP responses into a consistent JSON envelope:
  ```json
  {
    "success": true,
    "message": "Reserved Stock",
    "data": { ... },
    "timestamp": "2026-10-07T02:18:31"
  }
  ```

---

## 🛠️ Frameworks, Libraries & Tools Used

| Tool / Framework | Role & Purpose | Key Annotations / Usage |
| :--- | :--- | :--- |
| **Spring Boot 3.x** | Core application framework providing Web MVC, IoC Container, and Dependency Injection. | `@RestController`, `@Service`, `@RequiredArgsConstructor` |
| **Spring Data JPA & Hibernate** | Object-Relational Mapping (ORM) and data access abstraction over MS SQL Server. | `@Entity`, `@Table`, `@Id`, `@Column`, `@Embedded`, `@Embeddable` |
| **JPA Criteria API & Specifications** | Dynamic, type-safe programmatically built database queries without native SQL strings. | `JpaSpecificationExecutor`, `Specification<StockItem>`, `CriteriaBuilder.diff()` |
| **Jackson (`com.fasterxml.jackson`)** | Custom JSON serialization, field renaming, and ignore unknown properties. | `@JsonProperty("warehouse_name")`, `@JsonIgnoreProperties` |
| **Jakarta Validation (Hibernate Validator)** | Declarative REST input contract validation. | `@Valid`, `@NotEmpty`, `@NotNull`, `@Positive`, `@PositiveOrZero` |
| **Lombok** | Boilerplate code reduction for getters, setters, constructors, and builders. | `@Data`, `@RequiredArgsConstructor`, `@NoArgsConstructor` |

---

## 🔍 JPA Criteria API Specifications Deep Dive

Rather than creating combinatorial repository queries (`findByCategoryAndWarehouseLocationAndUnitPriceGreaterThan...`), dynamic inventory searching utilizes **Spring Data JPA Specifications**:

```java
// StockItemSpecification.java
public static Specification<StockItem> hasMinimumQuantityAvailable(int quantity) {
    return (root, query, cb) ->
            cb.greaterThanOrEqualTo(
                    cb.diff(root.get("quantityOnHand"), root.get("quantityReserved")),
                    quantity
            );
}
```

In `StockItemServiceImpl`, query parameters are composed conditionally at runtime:

```java
Specification<StockItem> spec = (root, query, cb) -> cb.conjunction();

if (category != null) spec = spec.and(StockItemSpecification.hasCategory(category));
if (minPrice != null) spec = spec.and(StockItemSpecification.priceGreaterThanOrEqualTo(minPrice));
if (maxPrice != null) spec = spec.and(StockItemSpecification.priceLessThanOrEqualTo(maxPrice));
if (minQuantityAvailable != null) spec = spec.and(StockItemSpecification.hasMinimumQuantityAvailable(minQuantityAvailable));

List<StockItem> results = repository.findAll(spec);
```

---

## 🌐 API Endpoints Reference

| # | Method | Path | Description | Sample Request Payload |
|---|--------|-------------------------------|----------------------------------|------------------------|
| 1 | `POST` | `/api/stock-items` | Onboard a new SKU | `{"sku": "SKU-101", "productName": "Motor", "category": "AUTOMATION", "quantityOnHand": 50, "warehouseLocation": "W-1", "unitPrice": 199.99, "packageLengthCm": 30, "packageWidthCm": 20, "packageHeightCm": 15}` |
| 2 | `GET` | `/api/stock-items/{sku}` | Fetch a single stock item by SKU | N/A |
| 3 | `GET` | `/api/stock-items` | Dynamic search/filter (`category`, `warehouseLocation`, `active`, `minPrice`, `maxPrice`, `minQuantityAvailable`) | Query Params: `?category=AUTOMATION&minPrice=100&minQuantityAvailable=10` |
| 4 | `PUT` | `/api/stock-items/{sku}/reserve` | Reserve stock against an order | `{"quantity": 5}` |
| 5 | `PATCH` | `/api/stock-items/{sku}` | Partial update (restock, price, location, or `active=false`) | `{"quantity_on_hand": 100, "unit_price": 249.99, "active": false}` |

---

## 🚀 Running the Project Locally

### Prerequisites
* **Java 17+**
* **Maven 3.8+**
* **MS SQL Server / SSMS**

### Application Configuration ([`application.yaml`](file:///d:/Learn/Spring%20Boot/Exercises/web/microservice-app/stock-reservation-service/src/main/resources/application.yaml))
Ensure database credentials are configured via environment variables or direct local values:
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=stock_db;encrypt=false;trustServerCertificate=true
    username: ${MY_SQL_SERVER_DB_USERNAME:sa}
    password: ${MY_SQL_SERVER_DB_PASSWORD:YourPassword123!}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

### Build & Run
```bash
# Build project
./mvnw clean package

# Run application
./mvnw spring-boot:run
```
