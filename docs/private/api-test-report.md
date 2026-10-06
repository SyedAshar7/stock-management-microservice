# Stock Reservation Microservice — API Test & Validation Report

## Executive Summary

This report documents the live API route testing conducted against the **Warehouse Stock Reservation Microservice** running locally on `http://localhost:8080`. 

Following code fixes applied by the engineering team (enabling `unitPrice` DTO mapping and fixing SKU deactivation via PATCH), a full rerun of the test suite was executed using fresh stock data (`SKU-NEW-24947`).

The test suite validated all 5 endpoints specified in [`stock-reservation-microservice-tasks.md`](file:///d:/Learn/Spring%20Boot/Exercises/web/microservice-app/stock-reservation-service/src/docs/stock-reservation-microservice-tasks.md).

---

## Overall Test Execution Results (Post-Fix Rerun)

| Endpoint Group | Method | Path | Test Count | Pass | Fail | Primary Status |
| :--- | :--- | :--- | :---: | :---: | :---: | :---: |
| **Task 1 & 2: Onboard SKU** | `POST` | `/api/stock-items` | 3 | 3 | 0 | HTTP 200 / 409 / 400 |
| **Task 2: Fetch by SKU** | `GET` | `/api/stock-items/{sku}` | 2 | 2 | 0 | HTTP 200 / 404 |
| **Task 3: Dynamic Search** | `GET` | `/api/stock-items` | 8 | 8 | 0 | HTTP 200 |
| **Task 4: Reserve Stock** | `PUT` | `/api/stock-items/{sku}/reserve` | 4 | 4 | 0 | HTTP 200 / 409 / 400 / 404 |
| **Task 5: Partial Update** | `PATCH` | `/api/stock-items/{sku}` | 4 | 4 | 0 | HTTP 200 |
| **TOTAL** | | | **21** | **21** | **0** | **100% Pass Rate** |

---

## Detailed Test Case Results & Response Payloads

### 1. POST `/api/stock-items` — Onboard New SKU

#### Test 1.1: Create Valid Stock Item (Fresh Data)
* **Request:** `POST http://localhost:8080/api/stock-items`
* **Body:**
  ```json
  {
    "sku": "SKU-NEW-24947",
    "productName": "Smart Conveyor Motor",
    "category": "AUTOMATION",
    "quantityOnHand": 40,
    "warehouseLocation": "WH-ZONE-C",
    "unitPrice": 499.95,
    "packageLengthCm": 50.00,
    "packageWidthCm": 25.00,
    "packageHeightCm": 35.00
  }
  ```
* **Status:** `HTTP 200 OK`
* **Response:**
  ```json
  {
    "success": true,
    "message": "Added Stock Item",
    "data": {
      "active": true,
      "category": "AUTOMATION",
      "last_restocked_date": null,
      "name": "Smart Conveyor Motor",
      "package_height": 35.00,
      "package_length": 50.00,
      "package_width": 25.00,
      "price": 499.95,
      "quantity_available": 40,
      "quantity_on_hand": 40,
      "quantity_reserved": 0,
      "sku": "SKU-NEW-24947",
      "warehouse_name": "WH-ZONE-C"
    },
    "timestamp": null
  }
  ```
* **Verdict:** **PASS** — Price correctly populated as `499.95`.

#### Test 1.2: Create Duplicate SKU
* **Request:** `POST http://localhost:8080/api/stock-items` (Same body as 1.1)
* **Status:** `HTTP 409 Conflict`
* **Response:**
  ```json
  {
    "success": false,
    "message": "Cannot create stock, SKU-NEW-24947sku already exists",
    "data": null,
    "timestamp": "2026-10-07T02:18:31"
  }
  ```
* **Verdict:** **PASS** — Handled by `DuplicateSkuException`.

#### Test 1.3: Create Invalid Payload (Empty SKU, negative values)
* **Request:** `POST http://localhost:8080/api/stock-items`
* **Status:** `HTTP 400 Bad Request`
* **Verdict:** **PASS** — Bean Validation triggered via `@Valid`.

---

### 2. GET `/api/stock-items/{sku}` — Fetch Stock Item by SKU

#### Test 2.1: Fetch Existing SKU
* **Request:** `GET http://localhost:8080/api/stock-items/SKU-NEW-24947`
* **Status:** `HTTP 200 OK`
* **Response:**
  ```json
  {
    "success": true,
    "message": "Fetched Stock Item",
    "data": {
      "active": true,
      "category": "AUTOMATION",
      "name": "Smart Conveyor Motor",
      "price": 499.95,
      "quantity_available": 40,
      "quantity_on_hand": 40,
      "quantity_reserved": 0,
      "sku": "SKU-NEW-24947",
      "warehouse_name": "WH-ZONE-C"
    }
  }
  ```
* **Verdict:** **PASS** — Correct item details returned.

#### Test 2.2: Fetch Non-existent SKU
* **Request:** `GET http://localhost:8080/api/stock-items/NON-EXISTENT-SKU-99999`
* **Status:** `HTTP 404 Not Found`
* **Verdict:** **PASS** — Handled by `StockItemNotFoundException`.

---

### 3. GET `/api/stock-items` — Dynamic Inventory Search / Filtering

| Test # | Filter Query | Expected Behavior | Actual Status | Result |
| :--- | :--- | :--- | :---: | :--- |
| **3.1** | None (All Items) | Return list of all stock items | `HTTP 200` | **PASS** |
| **3.2** | `category=AUTOMATION` | Return items in AUTOMATION category | `HTTP 200` | **PASS** |
| **3.3** | `warehouseLocation=WH-ZONE-C` | Return items at location WH-ZONE-C | `HTTP 200` | **PASS** |
| **3.4** | `minPrice=400` | Filter items with price >= 400 | `HTTP 200` | **PASS** |
| **3.5** | `maxPrice=600` | Filter items with price <= 600 | `HTTP 200` | **PASS** |
| **3.6** | `minPrice=400&maxPrice=600` | Filter items in price range | `HTTP 200` | **PASS** |
| **3.7** | `minQuantityAvailable=30` | Filter items with available stock >= 30 | `HTTP 200` | **PASS** |
| **3.8** | `category=NON_EXISTENT_CAT` | Return HTTP 200 with empty list `[]` | `HTTP 200` | **PASS** (`"data": []`) |

---

### 4. PUT `/api/stock-items/{sku}/reserve` — Stock Reservation

#### Test 4.1: Valid Stock Reservation
* **Request:** `PUT http://localhost:8080/api/stock-items/SKU-NEW-24947/reserve`
* **Body:** `{"quantity": 10}`
* **Status:** `HTTP 200 OK`
* **Response:**
  ```json
  {
    "success": true,
    "message": "Reserved Stock",
    "data": {
      "quantity_on_hand": 40,
      "quantity_reserved": 10,
      "quantity_available": 30
    }
  }
  ```
* **Verdict:** **PASS** — `quantity_reserved` updated to 10, `quantity_available` computed correctly as 30.

#### Test 4.2: Insufficient Stock Reservation
* **Request:** `PUT http://localhost:8080/api/stock-items/SKU-NEW-24947/reserve`
* **Body:** `{"quantity": 500}`
* **Status:** `HTTP 409 Conflict`
* **Verdict:** **PASS** — Handled by `InsufficientStockException`.

#### Test 4.3: Invalid Negative Quantity
* **Request:** `PUT http://localhost:8080/api/stock-items/SKU-NEW-24947/reserve`
* **Body:** `{"quantity": -5}`
* **Status:** `HTTP 400 Bad Request`
* **Verdict:** **PASS** — Handled by `@Valid`.

#### Test 4.4: Reserve Non-existent SKU
* **Request:** `PUT http://localhost:8080/api/stock-items/NON-EXISTENT-SKU-99999/reserve`
* **Body:** `{"quantity": 10}`
* **Status:** `HTTP 404 Not Found`
* **Verdict:** **PASS** — Handled by `StockItemNotFoundException`.

---

### 5. PATCH `/api/stock-items/{sku}` — Partial Updates / Deactivation

#### Test 5.1: Restock Quantity
* **Request:** `PATCH http://localhost:8080/api/stock-items/SKU-NEW-24947`
* **Body:** `{"quantity_on_hand": 150}`
* **Status:** `HTTP 200 OK`
* **Response:** `quantity_on_hand` updated to `150`, `quantity_available` recalculated to `140` (`150 - 10`).
* **Verdict:** **PASS**.

#### Test 5.2: Update Location & Price
* **Request:** `PATCH http://localhost:8080/api/stock-items/SKU-NEW-24947`
* **Body:** `{"unit_price": 549.99, "warehouse_location": "WH-ZONE-D"}`
* **Status:** `HTTP 200 OK`
* **Response:** `price` updated to `549.99`, `warehouse_location` updated to `"WH-ZONE-D"`.
* **Verdict:** **PASS**.

#### Test 5.3: Deactivate SKU (`active: false`)
* **Request:** `PATCH http://localhost:8080/api/stock-items/SKU-NEW-24947`
* **Body:** `{"active": false}`
* **Status:** `HTTP 200 OK`
* **Response Body:**
  ```json
  {
    "success": true,
    "message": "Updated stock",
    "data": {
      "active": false,
      "sku": "SKU-NEW-24947"
    }
  }
  ```
* **Verdict:** **PASS** — `active` field successfully updated to `false`.

#### Test 5.4: Verification GET Post-Deactivation
* **Request:** `GET http://localhost:8080/api/stock-items/SKU-NEW-24947`
* **Status:** `HTTP 200 OK`
* **Response:** `"active": false` confirmed in database state.
* **Verdict:** **PASS**.

---

## Verified Fixes Summary

1. **Price Mapping Fixed:** Unit price (`price`) is now populated across all DTO responses.
2. **SKU Deactivation Fixed:** `PATCH` endpoint now correctly processes `active: false` to deactivate items in DB.
