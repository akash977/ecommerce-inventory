# E-Commerce Inventory Management System

A production-ready Spring Boot backend application for managing inventory operations in an e-commerce system.  
The project focuses on concurrency-safe inventory reservations, transaction management, Redis caching, and REST API design.

---

# Tech Stack

| Component | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.0.6 |
| Database | H2 Database |
| Caching | Redis |
| Build Tool | Maven |
| API Documentation | Swagger / OpenAPI |
| Testing | JUnit 5, Mockito |
| Architecture | RESTful APIs |

---

# Features

## Inventory Supply Management
- Add stock for products
- Automatically updates available inventory

## Inventory Reservation
- Reserve product quantity safely
- Prevents overselling during concurrent requests

## Reservation Cancellation
- Supports partial/full reservation cancellation
- Restores inventory correctly

## Availability Tracking
- Query live available stock using SKU

## Concurrency Handling
- Implemented using Optimistic Locking (`@Version`)
- Prevents race conditions and overselling

## Redis Caching
- Frequently accessed inventory data cached
- Cache eviction handled on inventory updates

## Transaction Management
- Uses `@Transactional`
- Ensures database consistency during failures

## API Documentation
- Integrated Swagger UI for interactive API testing

---

# Project Structure

```text
src
 ├── main
 │   ├── java
 │   │   └── com.example.ecommerce
 │   │       ├── controller
 │   │       ├── service
 │   │       ├── repository
 │   │       ├── model
 │   │       ├── dto
 │   │       ├── exception
 │   │       └── config
 │   └── resources
 │       └── application.properties
 │
 └── test
     └── java
```

---

# API Endpoints

## 1. Create Inventory Supply

### Request
```http
POST /api/inventory/supply
```

### Sample Request Body
```json
{
  "sku": "IPH15",
  "itemName": "iPhone 15",
  "quantity": 100
}
```

---

## 2. Reserve Inventory

### Request
```http
POST /api/inventory/reserve
```

### Sample Request Body
```json
{
  "sku": "IPH15",
  "quantity": 5
}
```

---

## 3. Cancel Reservation

### Request
```http
POST /api/inventory/reservation/{id}/cancel
```

### Sample Request Body
```json
{
  "quantity": 2
}
```

---

## 4. Check Availability

### Request
```http
GET /api/inventory/availability?sku=IPH15
```

---

# Concurrency Handling

The system uses **Optimistic Locking** through the `@Version` annotation.

This ensures:
- Multiple users cannot reserve the same inventory simultaneously
- Prevents overselling
- Handles concurrent reservation requests safely

If concurrent modification occurs, Spring throws an `OptimisticLockException`.

---

# Redis Caching Strategy

Implemented caching for inventory availability APIs:

- `@Cacheable` → caches frequently requested inventory data
- `@CacheEvict` → clears stale cache entries after updates/reservations

Benefits:
- Faster API response time
- Reduced database load

---

# Transaction Management

All inventory write operations are wrapped using:

```java
@Transactional
```

This guarantees:
- Atomic database operations
- Automatic rollback on failure
- Consistent inventory state

---

# Database Schema

## INVENTORY Table

| Column | Description |
|---|---|
| id | Primary Key |
| sku | Unique Product SKU |
| item_name | Product Name |
| total_quantity | Total Added Quantity |
| available_quantity | Available Stock |
| reserved_quantity | Reserved Stock |
| version | Optimistic Lock Version |

---

## RESERVATION Table

| Column | Description |
|---|---|
| id | Primary Key |
| sku | Product SKU |
| quantity | Reserved Quantity |
| status | Reservation Status |
| version | Optimistic Lock Version |

---

# Test Coverage

The project contains:
- Service layer tests
- Controller layer tests
- Concurrency tests
- Transaction rollback tests

### Covered Scenarios
- Successful reservation
- Insufficient inventory
- Reservation cancellation
- Concurrent reservations
- Rollback during failures
- API validation errors

---

# How to Run

## Prerequisites

- Java 17+
- Maven
- Redis running locally on port `6379`

---

## Start Redis

### Using Docker

```bash
docker run -d -p 6379:6379 redis
```

---

# Run Application

```bash
git clone https://github.com/akash977/ecommerce-inventory.git

cd ecommerce-inventory

.\mvnw.cmd spring-boot:run
```

---

# Swagger UI

```text
http://localhost:8080/swagger-ui/index.html
```

---

# H2 Database Console

```text
http://localhost:8080/h2-console
```

### H2 Credentials

| Property | Value |
|---|---|
| JDBC URL | jdbc:h2:mem:ecommercedb |
| Username | sa |
| Password | (leave empty) |

---

# Build Commands

## Run Tests

```bash
.\mvnw.cmd test
```

## Full Build

```bash
.\mvnw.cmd clean install
```

## Skip Tests

```bash
.\mvnw.cmd clean install -DskipTests
```


# Author

Akash Kumar Gupta

GitHub:
https://github.com/akash977
