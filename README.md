# Stock Sync Service

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen)
![Maven](https://img.shields.io/badge/Maven-3.9+-red)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED)

A Spring Boot microservice that periodically synchronizes product inventory from multiple vendors into a centralized product catalog.

The service simulates a real-world e-commerce dropshipping platform where inventory is collected from multiple suppliers, normalized into a common data model, stored in a relational database, and exposed through a REST API. The implementation emphasizes clean architecture, maintainability, resilience, automated quality gates, and containerized deployment.

---

# Architecture

```text
                 Vendor A (REST API)
                        │
                        ▼
                   WireMock
                        │
                        │
 Vendor B (CSV) ────────┤
                        ▼
               Stock Sync Service
                        │
          +-------------+-------------+
          │                           │
          ▼                           ▼
    PRODUCT_STOCK               STOCK_EVENT
          │
          ▼
 GET /api/v1/products
```

---

# Highlights

* Java 17
* Spring Boot 3
* Layered Architecture (Controller, Service, Repository)
* Scheduled synchronization using `@Scheduled`
* Vendor A integration through WireMock
* Vendor B integration through CSV
* H2 in-memory database
* REST API with API versioning (`/api/v1`)
* Swagger / OpenAPI documentation
* Spring Boot Actuator
* Resilience4j (Retry and Circuit Breaker)
* Docker & Docker Compose
* Spring Profiles (`dev`, `test`, `prod`)
* Docker Compose Profiles
* Unit & Integration Tests
* PMD
* SpotBugs
* FindSecBugs
* Sonatype OSS Index Dependency Audit
* Postman Collection
* User Acceptance Testing (UAT)

---

# Prerequisites

Install the following before running the project.

| Software       | Version                      |
| -------------- | ---------------------------- |
| Java           | 17 or later                  |
| Maven          | 3.9 or later                 |
| Docker Desktop | Latest                       |
| Docker Compose | Included with Docker Desktop |
| Git            | Latest                       |

Optional:

* IntelliJ IDEA
* Postman

Verify your installation:

```bash
java -version
mvn -version
docker --version
docker compose version
```

---

# Environment Variables

The project uses two categories of environment variables:

* **Build-time variables** used during the Maven verification phase.
* **Runtime variables** used by the Spring Boot application.

## Build-Time Variables (Sonatype OSS Index)

The Maven build includes the **Sonatype OSS Index Maven Plugin**, which performs a dependency vulnerability audit during the **`verify`** phase.

Unlike PMD, SpotBugs, and FindSecBugs, which analyze your source code locally, Sonatype OSS Index analyzes the project's third-party dependencies against the Sonatype vulnerability database.

The project is configured to fail the build when a runtime dependency contains a vulnerability with a **CVSS score of 7.0 or higher**.

Because the audit communicates with Sonatype's service, valid credentials are required.

### Required Environment Variables

| Variable         | Description                                      |
| ---------------- | ------------------------------------------------ |
| `SONATYPE_EMAIL` | Email address used to register at Sonatype Guide |
| `SONATYPE_TOKEN` | Sonatype Guide Personal Access Token (PAT)       |

### Creating a Sonatype Personal Access Token

1. Sign in to **https://guide.sonatype.com**
2. Open **Settings**
3. Navigate to **Personal Access Tokens**
4. Click **Generate New Token**
5. Give the token a descriptive name
6. Click **Create**
7. Copy the token immediately

> **Note**
>
> The Personal Access Token is displayed only once. If it is lost, a new token must be generated.

### Configure Environment Variables

#### Windows PowerShell

```powershell
$env:SONATYPE_EMAIL="your-email@example.com"
$env:SONATYPE_TOKEN="your-personal-access-token"
```

#### Windows Command Prompt

```cmd
set SONATYPE_EMAIL=your-email@example.com
set SONATYPE_TOKEN=your-personal-access-token
```

#### Linux / macOS

```bash
export SONATYPE_EMAIL="your-email@example.com"
export SONATYPE_TOKEN="your-personal-access-token"
```

After configuring the variables, verify the project:

```bash
mvn clean verify
```

The Maven verification phase executes the project's quality gate:

* Unit & Integration Tests
* PMD
* SpotBugs
* FindSecBugs
* Sonatype OSS Index Dependency Audit

> **Note**
>
> `mvn clean package` does **not** execute the Sonatype OSS Index audit and therefore does not require these credentials.

---

## Runtime Variables

Runtime variables are already configured by Docker Compose.

| Variable                       | Purpose                           |
| ------------------------------ | --------------------------------- |
| `SPRING_PROFILES_ACTIVE`       | Selects the active Spring profile |
| `STOCK_SYNC_VENDOR_A_BASE_URL` | Vendor A base URL                 |
| `STOCK_SYNC_VENDOR_B_CSV_PATH` | Vendor B CSV file path            |

Normally, these do not need to be configured manually when using Docker Compose.

---

# Quick Start

## 1. Verify the project

```bash
mvn clean verify
```

## 2. Build the application

```bash
mvn clean package
```

## 3. Start the development environment

```bash
docker compose --profile dev up --build
```

The following services will be available:

| Service             | URL                                         |
| ------------------- | ------------------------------------------- |
| Products API        | http://localhost:8080/api/v1/products       |
| Swagger UI          | http://localhost:8080/swagger-ui/index.html |
| Health              | http://localhost:8080/actuator/health       |
| H2 Console          | http://localhost:8080/h2-console            |
| Vendor A (WireMock) | http://localhost:8089/vendor-a/products     |

To stop the development environment:

```bash
docker compose --profile dev down
```
---

# Configuration

## Spring Profiles

The application uses Spring Profiles to separate configuration for different environments.

| Profile | Purpose                                                             |
| ------- | ------------------------------------------------------------------- |
| `dev`   | Local development with developer-friendly configuration and logging |
| `test`  | Test environment configuration                                      |
| `prod`  | Production-like runtime configuration                               |

The active profile is selected using:

```text
SPRING_PROFILES_ACTIVE
```

When running with Docker Compose, this variable is configured automatically.

---

## Docker Compose Profiles

Docker Compose profiles start the appropriate application container and automatically activate the matching Spring profile.

| Docker Compose Profile | Spring Profile |
| ---------------------- | -------------- |
| `dev`                  | `dev`          |
| `test`                 | `test`         |
| `prod`                 | `prod`         |

### Development

Start:

```bash
docker compose --profile dev up --build
```

Stop:

```bash
docker compose --profile dev down
```

### Test

Start:

```bash
docker compose --profile test up --build
```

Stop:

```bash
docker compose --profile test down
```

### Production

Start:

```bash
docker compose --profile prod up --build
```

Stop:

```bash
docker compose --profile prod down
```

> **Important**
>
> Run only one application profile at a time. Since each profile exposes the application on port **8080**, multiple profiles cannot run simultaneously.

---

# Build Commands

The project follows the standard Maven lifecycle.

### Clean

Removes previously generated build artifacts.

```bash
mvn clean
```

### Verify

Compiles the project, runs automated tests, executes static analysis, and performs the Sonatype OSS Index dependency audit.

```bash
mvn clean verify
```

### Package

Builds the executable Spring Boot JAR.

```bash
mvn clean package
```

Generated artifact:

```text
target/StockSyncService-1.0.0.jar
```

---

# Running the Application

## Option 1 — IntelliJ IDEA (Recommended)

Run the Spring Boot application using the desired Spring profile.

For local development:

```text
dev
```

---

## Option 2 — Maven

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## Option 3 — Docker Compose

Development:

```bash
docker compose --profile dev up --build
```

Testing:

```bash
docker compose --profile test up --build
```

Production:

```bash
docker compose --profile prod up --build
```

---

# Docker

The project includes:

* Multi-stage Dockerfile
* Docker Compose
* Profile-based container startup
* Volume mounting for Vendor B CSV
* WireMock container for Vendor A simulation

The Dockerfile is optimized for layer caching to reduce rebuild time by separating dependency resolution from application source code.

---

# REST API

## Products

Returns the latest synchronized inventory.

```http
GET /api/v1/products
```

Example:

```text
GET http://localhost:8080/api/v1/products
```

---

# Available Endpoints

| Endpoint                                            | Description                  |
| --------------------------------------------------- | ---------------------------- |
| `GET /api/v1/products`                              | Latest synchronized products |
| `GET /actuator/health`                              | Application health           |
| `GET /swagger-ui/index.html`                        | Swagger UI                   |
| `GET /v3/api-docs`                                  | OpenAPI specification        |
| `GET /h2-console`                                   | H2 database console          |
| `GET http://localhost:8089/vendor-a/products`       | Vendor A (WireMock)          |
| `POST http://localhost:8089/__admin/mappings/reset` | Reload WireMock mappings     |

---

# Swagger / OpenAPI

Interactive API documentation is available through Swagger UI.

```
http://localhost:8080/swagger-ui/index.html
```

The generated OpenAPI specification is available at:

```
http://localhost:8080/v3/api-docs
```

Swagger can be used to explore the API, inspect request/response models, and execute requests directly from the browser.

---

# Actuator

Spring Boot Actuator provides operational information about the application.

Health endpoint:

```
http://localhost:8080/actuator/health
```

Example response:

```json
{
  "status": "UP"
}
```

---

# Vendor Simulation

## Vendor A

Vendor A is simulated using **WireMock**.

Endpoint:

```
http://localhost:8089/vendor-a/products
```

To modify Vendor A data:

1. Edit the WireMock response under the `wiremock/` directory.
2. Reset the mappings:

```http
POST http://localhost:8089/__admin/mappings/reset
```

3. Wait for the next scheduled synchronization.

---

## Vendor B

Vendor B is simulated using a local CSV file.

```
vendor-b/stock.csv
```

Expected format:

```csv
sku,name,quantity
B-100,Vendor B Monitor,15
B-200,Vendor B Dock,7
SKU-004,USB-C Docking Station,8
```

The file is mounted into the application container through Docker Compose.

After modifying the CSV file, wait for the next scheduled synchronization.

---

### Modifying Test Data

During development and User Acceptance Testing (UAT), vendor data can be modified directly from the IDE without rebuilding the application.

#### Vendor A (WireMock)

1. Open the WireMock response JSON under the `wiremock/` directory.
2. Edit the product data (for example, change a stock quantity or add a new product).
3. Save the file.
4. Reload the WireMock mappings:

```http
POST http://localhost:8089/__admin/mappings/reset
```

5. Wait for the next scheduled synchronization cycle.
6. Verify the changes using:

    * `GET /api/v1/products`
    * H2 Console (`PRODUCT_STOCK` and `STOCK_EVENT`)

#### Vendor B (CSV)

1. Open:

```text
vendor-b/stock.csv
```

2. Edit the CSV data directly in the IDE.
3. Save the file.
4. Wait for the next scheduled synchronization cycle.
5. Verify the changes using:

    * `GET /api/v1/products`
    * H2 Console (`PRODUCT_STOCK` and `STOCK_EVENT`)

> **Note**
>
> The Vendor B CSV file is mounted into the Docker container as a read-only volume. Saving changes in the local file immediately makes the updated content available to the running container; no container restart is required.

---
# H2 Database

The application stores synchronized inventory and stock events in an H2 in-memory database.

Open:

```
http://localhost:8080/h2-console
```

Connection details are defined in the active Spring profile configuration.

Useful queries:

```sql
SELECT * FROM PRODUCT_STOCK;
```

```sql
SELECT * FROM STOCK_EVENT;
```

These tables can be used to verify synchronization results and out-of-stock event generation.

---

### Accessing the H2 Console

Open the H2 Console:

```text
http://localhost:8080/h2-console
```

Use the following connection settings.

| Setting  | Value                                                                            |
| -------- | -------------------------------------------------------------------------------- |
| JDBC URL | `jdbc:h2:mem:stocksync` *(or the value configured in the active Spring profile)* |
| Username | `sa`                                                                             |
| Password | *(leave blank unless configured otherwise)*                                      |

Click **Connect**.

Useful queries:

```sql
SELECT * FROM PRODUCT_STOCK;
```

```sql
SELECT * FROM STOCK_EVENT;
```

These tables can be used to verify the latest synchronized inventory and generated out-of-stock events during development and UAT.

---
# Postman Collection

A ready-to-use Postman collection is included in the repository:

```
Postman/
└── StockSyncService.postman_collection.json
```

Import the collection into Postman to test the service without creating requests manually.

The collection includes:

* Products API
* Health endpoint
* Vendor A mock endpoint
* WireMock mapping reset endpoint

---

# Operational Behavior

The application periodically synchronizes inventory from two independent vendor sources into a centralized product catalog.

Each synchronization cycle performs the following steps:

1. Retrieve the latest inventory from Vendor A (REST API via WireMock).
2. Read the latest inventory from Vendor B (local CSV file).
3. Normalize vendor-specific data into a common domain model.
4. Insert new products.
5. Update existing products.
6. Detect products transitioning from **in stock** to **out of stock**.
7. Persist out-of-stock events.
8. Expose the latest inventory through the REST API.

```text id="mwbg7t"
Vendor A + Vendor B
         │
         ▼
 Scheduled Synchronization
         │
         ▼
 Product Normalization
         │
         ▼
   PRODUCT_STOCK
         │
         ├──────────────┐
         ▼              ▼
 REST API         STOCK_EVENT
```

> **Note**
>
> The `GET /api/v1/products` endpoint is **read-only**. It returns the latest synchronized data from the database and does not trigger a synchronization.

---

# Business Rules

The following business rules were implemented based on the coding challenge requirements.

| Rule                       | Behavior                                                    |
| -------------------------- | ----------------------------------------------------------- |
| Product identity           | Products are uniquely identified by **SKU + Vendor**        |
| Synchronization            | Each run processes the vendor's complete inventory snapshot |
| New product                | Insert into `PRODUCT_STOCK`                                 |
| Existing product           | Update inventory and timestamp                              |
| Positive → Zero            | Create an `OUT_OF_STOCK` event                              |
| Zero → Zero                | No additional event                                         |
| Zero → Positive            | Update inventory (restock)                                  |
| Positive → Positive        | Update inventory only                                       |
| Same SKU, different vendor | Stored as separate products                                 |

Out-of-stock events are generated only when the following condition is true:

```text id="tr7ysd"
Previous Quantity > 0
```

This prevents duplicate out-of-stock events for products that remain unavailable.

---

# Quality Gate

Project quality is enforced during the Maven verification phase.

```text id="g9frjv"
mvn clean verify
        │
        ├── Unit Tests
        ├── PMD
        ├── SpotBugs
        ├── FindSecBugs
        └── Sonatype OSS Index Audit
```

Each tool has a specific responsibility.

| Tool                     | Purpose                                              |
| ------------------------ | ---------------------------------------------------- |
| Unit & Integration Tests | Verify application correctness                       |
| PMD                      | Code quality and maintainability                     |
| SpotBugs                 | Detect potential implementation defects              |
| FindSecBugs              | Detect common security issues                        |
| Sonatype OSS Index       | Audit runtime dependencies for known vulnerabilities |

The Sonatype OSS Index audit is configured to fail the build when a runtime dependency contains a vulnerability with a **CVSS score of 7.0 or higher**.

---

# User Acceptance Testing (UAT)

The following functional scenarios were successfully validated during development.

| Scenario                          | Status |
| --------------------------------- | :----: |
| Application startup               |    ✅   |
| Products endpoint                 |    ✅   |
| Vendor A synchronization          |    ✅   |
| Vendor B synchronization          |    ✅   |
| Insert new product                |    ✅   |
| Update existing product           |    ✅   |
| Out-of-stock transition           |    ✅   |
| Zero → Zero transition            |    ✅   |
| Zero → Positive transition        |    ✅   |
| Same SKU across different vendors |    ✅   |
| Vendor failure handling           |    ✅   |

Verification was performed using:

* Postman
* H2 Console
* WireMock
* Vendor B CSV
* Application logs

---

# Project Structure

```text id="8j8dfx"
StockSyncService
├── src/
│   ├── main/
│   └── test/
├── vendor-b/
├── wiremock/
├── Postman/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

Key directories:

| Directory  | Purpose                    |
| ---------- | -------------------------- |
| `src/main` | Application source code    |
| `src/test` | Unit and integration tests |
| `vendor-b` | Vendor B CSV simulation    |
| `wiremock` | Vendor A mock API          |
| `Postman`  | API testing collection     |

---

# Design Decisions

Several implementation decisions were made intentionally to balance simplicity, maintainability, and extensibility.

* **Layered Architecture** separates HTTP, business logic, persistence, and vendor integrations.
* **Vendor-specific clients** isolate external integrations from business logic.
* **DTOs** prevent direct exposure of persistence entities through the REST API.
* **Spring Profiles** separate development, testing, and production configuration.
* **Docker Compose Profiles** provide consistent runtime environments.
* **Resilience4j** improves robustness against temporary vendor failures.
* **Swagger/OpenAPI** provides self-documenting APIs.
* **API Versioning** (`/api/v1`) supports future API evolution without breaking existing clients.
* **Stock Events** are persisted to provide an auditable history of out-of-stock transitions instead of relying solely on application logs.

These decisions were made to keep the solution aligned with common enterprise Spring Boot development practices while remaining faithful to the scope of the coding challenge.

---

# Potential Enhancements

The current implementation fulfills the challenge requirements while emphasizing clean architecture, maintainability, and developer experience.

If the service were to evolve into a production system, the following enhancements would be considered:

* Replace the H2 in-memory database with PostgreSQL or MySQL.
* Manage database schema evolution using Flyway or Liquibase.
* Add authentication and authorization for REST endpoints.
* Support pagination, filtering, and sorting for product queries.
* Expose a manual synchronization endpoint for operational use.
* Publish stock events to a message broker such as Apache Kafka or RabbitMQ.
* Add metrics and dashboards using Micrometer and Prometheus.
* Integrate distributed tracing for improved observability.
* Introduce distributed scheduler locking for multi-instance deployments.
* Use Testcontainers for integration testing against production-like infrastructure.

---

# Troubleshooting

## `mvn clean verify` fails

Ensure the required Sonatype environment variables are configured correctly.

```text id="0s3w1t"
SONATYPE_EMAIL
SONATYPE_TOKEN
```

Verify the variables are available in your shell before running Maven.

---

## Docker container starts with the wrong profile

Start the application using the appropriate Docker Compose profile.

Development:

```bash id="0w8qzy"
docker compose --profile dev up --build
```

Production:

```bash id="zxrhf8"
docker compose --profile prod up --build
```

The active Spring profile is reported in the application startup logs.

---

## WireMock changes are not reflected

After modifying a WireMock response, reload the mappings.

```http id="te9mpf"
POST http://localhost:8089/__admin/mappings/reset
```

Then wait for the next scheduled synchronization cycle.

---

## CSV changes are not reflected

Verify that:

* The CSV file was saved successfully.
* The file format remains valid.
* The scheduler has completed another synchronization cycle.

---

# Useful Commands

| Action            | Command                                    |
| ----------------- | ------------------------------------------ |
| Verify project    | `mvn clean verify`                         |
| Build application | `mvn clean package`                        |
| Start development | `docker compose --profile dev up --build`  |
| Stop development  | `docker compose --profile dev down`        |
| Start test        | `docker compose --profile test up --build` |
| Stop test         | `docker compose --profile test down`       |
| Start production  | `docker compose --profile prod up --build` |
| Stop production   | `docker compose --profile prod down`       |

---

# Challenge Requirements Mapping

| Requirement               | Implementation                |
| ------------------------- | ----------------------------- |
| Java 17+ and Spring Boot  | ✅ Java 17 / Spring Boot 3     |
| Scheduled synchronization | ✅ `@Scheduled`                |
| Vendor A REST API         | ✅ WireMock                    |
| Vendor B CSV              | ✅ Local mounted CSV           |
| Relational database       | ✅ H2                          |
| Product persistence       | ✅ `PRODUCT_STOCK`             |
| Out-of-stock detection    | ✅ `STOCK_EVENT`               |
| REST endpoint             | ✅ `GET /api/v1/products`      |
| Swagger/OpenAPI           | ✅ Springdoc OpenAPI           |
| Docker support            | ✅ Dockerfile & Docker Compose |
| Unit / Integration Tests  | ✅ Implemented                 |
| README                    | ✅ Included                    |

---

# Engineering Notes

This project was intentionally designed to demonstrate backend engineering practices rather than maximize feature count.

The implementation emphasizes:

* Clean layered architecture
* Separation of concerns
* Resilient external integrations
* Automated quality gates
* API documentation
* Containerized deployment
* Environment-specific configuration
* Developer onboarding

The goal was to produce a solution that is straightforward to understand, easy to extend, and representative of modern Spring Boot development practices while remaining faithful to the original coding challenge requirements.

---

## License

This project is licensed under the Apache License 2.0.

You are free to use, modify, and distribute this software in accordance with the terms of the Apache License 2.0.

For the full license text, see the LICENSE file included in this repository.
