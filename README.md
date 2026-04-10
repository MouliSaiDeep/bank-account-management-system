# Bank Account Management System

Event-sourced and CQRS-based Bank Account Management API built with Spring Boot and PostgreSQL.

The write model persists immutable domain events. The read model is maintained through projections for fast account and transaction queries.

## Tech Stack

- Java 21
- Spring Boot 3.5.13
- Spring Web (REST API)
- Spring Data JPA
- Spring Validation (Jakarta Validation)
- PostgreSQL 15
- Maven (wrapper included)
- Docker and Docker Compose

## Architecture

- Event Sourcing
- Every state-changing operation writes an immutable event to `events`.
- Account state is reconstructed by replaying events (with snapshot optimization).

- CQRS
- Command endpoints write to event store and update projections.
- Query endpoints read from projection tables only.

- Snapshotting
- Snapshot is refreshed every 50 events per account.
- Snapshots are stored in `snapshots` and used to reduce replay cost.

## Data Model

Schema bootstrap SQL is in `seeds/001_schema.sql`.

- `events`
- `snapshots`
- `account_summaries`
- `transaction_history`
- `projection_cursors`

## Prerequisites

- JDK 21
- Maven 3.9+ (optional, wrapper is included)
- Docker Desktop (for container-based run)

## Environment Variables

Copy `.env.example` to `.env` and adjust values if needed.

```env
API_PORT=8080
DATABASE_URL=jdbc:postgresql://db:5432/bank_db
DB_USER=bank_user
DB_PASSWORD=bank_password
DB_NAME=bank_db
```

For local (non-compose) app run, set `DATABASE_URL` to point to localhost:

```env
DATABASE_URL=jdbc:postgresql://localhost:5432/bank_db
```

## Run Locally (Without Docker Compose)

### 1. Start PostgreSQL

Use your own PostgreSQL instance, or start one quickly with Docker:

```bash
docker run --name bank-postgres \
	-e POSTGRES_USER=bank_user \
	-e POSTGRES_PASSWORD=bank_password \
	-e POSTGRES_DB=bank_db \
	-p 5432:5432 \
	-v ${PWD}/seeds:/docker-entrypoint-initdb.d \
	-d postgres:15
```

On Windows PowerShell, use:

```powershell
docker run --name bank-postgres `
	-e POSTGRES_USER=bank_user `
	-e POSTGRES_PASSWORD=bank_password `
	-e POSTGRES_DB=bank_db `
	-p 5432:5432 `
	-v ${PWD}/seeds:/docker-entrypoint-initdb.d `
	-d postgres:15
```

### 2. Export environment variables

PowerShell:

```powershell
$env:API_PORT="8080"
$env:DB_USER="bank_user"
$env:DB_PASSWORD="bank_password"
$env:DB_NAME="bank_db"
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/bank_db"
```

### 3. Start the Spring Boot app

```powershell
.\mvnw.cmd spring-boot:run
```

### 4. Health check

```bash
curl http://localhost:8080/health
```

Expected response:

```json
{ "status": "UP" }
```

## Run with Docker Compose

### 1. Create `.env`

```bash
cp .env.example .env
```

On Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

### 2. Start services

```bash
docker-compose up --build
```

### 3. Stop services

```bash
docker-compose down
```

## API Reference with cURL

Base URL:

```bash
http://localhost:8080
```

Set reusable shell variables (optional):

```bash
BASE_URL="http://localhost:8080"
ACCOUNT_ID="acc-test-12345"
```

### 1. Health

```bash
curl -X GET "$BASE_URL/health"
```

### 2. Create Account

```bash
curl -X POST "$BASE_URL/api/accounts" \
	-H "Content-Type: application/json" \
	-d '{
		"accountId": "acc-test-12345",
		"ownerName": "Jane Doe",
		"initialBalance": 100.00,
		"currency": "USD"
	}'
```

Expected status: `202 Accepted`

### 3. Deposit Money

```bash
curl -X POST "$BASE_URL/api/accounts/acc-test-12345/deposit" \
	-H "Content-Type: application/json" \
	-d '{
		"amount": 50.25,
		"description": "Salary credit",
		"transactionId": "txn-dep-0001"
	}'
```

Expected status: `202 Accepted`

### 4. Withdraw Money

```bash
curl -X POST "$BASE_URL/api/accounts/acc-test-12345/withdraw" \
	-H "Content-Type: application/json" \
	-d '{
		"amount": 20.00,
		"description": "ATM withdrawal",
		"transactionId": "txn-wd-0001"
	}'
```

Expected status: `202 Accepted`

### 5. Close Account

Account can be closed only when balance is `0`.

```bash
curl -X POST "$BASE_URL/api/accounts/acc-test-12345/close" \
	-H "Content-Type: application/json" \
	-d '{
		"reason": "Customer requested closure"
	}'
```

Expected status: `202 Accepted`

### 6. Get Account Summary (Projection)

```bash
curl -X GET "$BASE_URL/api/accounts/acc-test-12345"
```

### 7. Get Event Stream (Audit)

```bash
curl -X GET "$BASE_URL/api/accounts/acc-test-12345/events"
```

### 8. Time-Travel Balance Query

Timestamp in path must be URL-encoded ISO-8601.

Example timestamp:

- raw: `2026-04-10T15:30:00Z`
- encoded: `2026-04-10T15%3A30%3A00Z`

```bash
curl -X GET "$BASE_URL/api/accounts/acc-test-12345/balance-at/2026-04-10T15%3A30%3A00Z"
```

### 9. Get Paginated Transactions

```bash
curl -X GET "$BASE_URL/api/accounts/acc-test-12345/transactions?page=1&pageSize=10"
```

### 10. Rebuild Projections

```bash
curl -X POST "$BASE_URL/api/projections/rebuild"
```

Expected response:

```json
{
  "message": "Projection rebuild initiated."
}
```

### 11. Get Projection Status

```bash
curl -X GET "$BASE_URL/api/projections/status"
```

## Common HTTP Status Codes

- `200 OK`: successful query
- `202 Accepted`: command accepted
- `400 Bad Request`: invalid payload/inputs
- `404 Not Found`: account or resource not found
- `409 Conflict`: business-rule conflict (insufficient funds, account closed, duplicate account)

## Build and Test

```powershell
.\mvnw.cmd test
```

## Notes

- Projections are updated synchronously after event persistence in this implementation.
- Snapshot serialization stores balance as string for lossless decimal recovery.
