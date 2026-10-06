# Warehouse Stock Reservation Microservice

A Spring Boot microservice for warehouse stock management. It supports onboarding SKUs, searching inventory via dynamic filters, reserving stock for orders, restocking items, and deactivating discontinued SKUs.

## Architecture & Layers

The project uses a standard layered Spring Boot architecture:

- **Controller (`controller/`)**: Exposes REST endpoints (`/api/stock-items`), validates requests using `@Valid`, and delegates business operations to the service layer. `BaseController.java` handles global exceptions via `@RestControllerAdvice`.
- **Service (`service/`)**: Implements business logic (SKU uniqueness checks, availability calculations, stock reservation, restocking, and DTO/entity mapping).
- **Repository & Specification (`repository/`, `specification/`)**: Data persistence using Spring Data JPA. `StockItemSpecification.java` builds dynamic search filters using the JPA Criteria API.
- **Domain & Embeddable (`model/`, `embeddable/`)**: `StockItem` JPA entity mapped to table `stock_item`, embedding `PackageDimensions` via `@Embedded`.

## Layer Communication (DTOs vs Entities)

API clients interact exclusively through DTOs to separate the external HTTP API contract from database entities:

- **Inbound DTOs**: `CreateStockItemRequest`, `ReserveStockRequest`, and `StockItemPatchRequest` handle request validation (`@NotEmpty`, `@Positive`, etc.).
- **Outbound DTO**: `StockItemResponse` maps output fields using `@JsonProperty` (with `quantity_available` computed dynamically).
- **Response Envelope**: `ApiResponse<T>` wraps success and error responses into a consistent JSON envelope (`success`, `message`, `data`, `timestamp`).

## Technologies Used

- **Spring Boot 3.x** (Spring Web, Spring Data JPA, Validation)
- **Hibernate / Relational DB** (MS SQL Server)
- **JPA Criteria API** (Dynamic search via `JpaSpecificationExecutor`)
- **Jackson** (JSON property serialization and formatting)
- **Lombok** (Boilerplate getter/setter and constructor generation)

## API Endpoints

| Method | Path | Description | Sample Request |
|---|---|---|---|
| POST | `/api/stock-items` | Onboard a new SKU | `{"sku": "SKU-101", "productName": "Motor", "category": "HARDWARE", "quantityOnHand": 50, "warehouseLocation": "A-1", "unitPrice": 199.99, "packageLengthCm": 30, "packageWidthCm": 20, "packageHeightCm": 15}` |
| GET | `/api/stock-items/{sku}` | Fetch item by SKU | N/A |
| GET | `/api/stock-items` | Filter inventory (`category`, `warehouseLocation`, `active`, `minPrice`, `maxPrice`, `minQuantityAvailable`) | Query: `?category=HARDWARE&minPrice=100` |
| PUT | `/api/stock-items/{sku}/reserve` | Reserve stock quantity | `{"quantity": 5}` |
| PATCH | `/api/stock-items/{sku}` | Partial update (restock, price, location, active) | `{"quantity_on_hand": 100, "active": false}` |

## Running Locally

1. Configure database connection credentials in `src/main/resources/application.yaml`.
2. Build and start the service:
   ```bash
   ./mvnw spring-boot:run
   ```
