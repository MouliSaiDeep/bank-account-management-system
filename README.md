# Bank Account Management System

Event-sourced and CQRS-based bank account API using Spring Boot and PostgreSQL.

## Tech Stack

- Java 21
- Spring Boot 3.5.13
- Spring Web
- Spring Data JPA
- Spring Validation (Jakarta Validation)
- PostgreSQL 15
- Maven Wrapper (`mvnw`, `mvnw.cmd`)
- Docker + Docker Compose

## Core Architecture

- Event Sourcing: all state changes are immutable events in `events`.
- CQRS: command endpoints write events; query endpoints read projections.
- Projections: `account_summaries` and `transaction_history`.
- Snapshotting: snapshot refresh every 50 events per account.
- Schema bootstrap: [seeds/001_schema.sql](seeds/001_schema.sql).

## Environment Variables

Copy `.env.example` to `.env`.

```env
API_PORT=8080
DATABASE_URL=jdbc:postgresql://db:5432/bank_db
DB_USER=bank_user
DB_PASSWORD=bank_password
DB_NAME=bank_db
```

For local non-compose run, use:

```env
DATABASE_URL=jdbc:postgresql://localhost:5432/bank_db
```

## Run Locally

1. Start PostgreSQL (local installation or container).

```powershell
docker run --name bank-postgres `
  -e POSTGRES_USER=bank_user `
  -e POSTGRES_PASSWORD=bank_password `
  -e POSTGRES_DB=bank_db `
  -p 5432:5432 `
  -v ${PWD}/seeds:/docker-entrypoint-initdb.d `
  -d postgres:15
```

2. Set env vars in PowerShell.

```powershell
$env:API_PORT="8080"
$env:DB_USER="bank_user"
$env:DB_PASSWORD="bank_password"
$env:DB_NAME="bank_db"
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/bank_db"
```

3. Run application.

```powershell
.\mvnw.cmd spring-boot:run
```

4. Health check.

```bash
curl http://localhost:8080/health
```

## Run with Docker Compose

1. Create `.env`.

```powershell
Copy-Item .env.example .env
```

2. Start services.

```bash
docker-compose up --build
```

3. Stop services.

```bash
docker-compose down
```

## API Endpoints and cURL

Set a base URL (optional):

```bash
BASE_URL="http://localhost:8080"
```

1. Health

```bash
curl -X GET "$BASE_URL/health"
```

2. Create account (`POST /api/accounts`)

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

3. Deposit (`POST /api/accounts/{accountId}/deposit`)

```bash
curl -X POST "$BASE_URL/api/accounts/acc-test-12345/deposit" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 50.25,
    "description": "Salary credit",
    "transactionId": "txn-dep-0001"
  }'
```

4. Withdraw (`POST /api/accounts/{accountId}/withdraw`)

```bash
curl -X POST "$BASE_URL/api/accounts/acc-test-12345/withdraw" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 20.00,
    "description": "ATM withdrawal",
    "transactionId": "txn-wd-0001"
  }'
```

5. Close account (`POST /api/accounts/{accountId}/close`)

```bash
curl -X POST "$BASE_URL/api/accounts/acc-test-12345/close" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Customer requested closure"
  }'
```

6. Get account summary (`GET /api/accounts/{accountId}`)

```bash
curl -X GET "$BASE_URL/api/accounts/acc-test-12345"
```

7. Get event stream (`GET /api/accounts/{accountId}/events`)

```bash
curl -X GET "$BASE_URL/api/accounts/acc-test-12345/events"
```

8. Time-travel balance (`GET /api/accounts/{accountId}/balance-at/{timestamp}`)

Use URL-encoded ISO timestamp in path.
Example encoded timestamp: `2026-04-10T15%3A30%3A00Z`

```bash
curl -X GET "$BASE_URL/api/accounts/acc-test-12345/balance-at/2026-04-10T15%3A30%3A00Z"
```

9. Get paginated transactions (`GET /api/accounts/{accountId}/transactions`)

```bash
curl -X GET "$BASE_URL/api/accounts/acc-test-12345/transactions?page=1&pageSize=10"
```

10. Rebuild projections (`POST /api/projections/rebuild`)

```bash
curl -X POST "$BASE_URL/api/projections/rebuild"
```

11. Projection status (`GET /api/projections/status`)

```bash
curl -X GET "$BASE_URL/api/projections/status"
```

## HTTP Status Codes

- `200 OK` query success
- `202 Accepted` command accepted
- `400 Bad Request` invalid payload/input
- `404 Not Found` resource not found
- `409 Conflict` business rule conflict

## Build and Test

```powershell
.\mvnw.cmd test
```

## Submission Notes

- Projections are synchronous in this implementation.
- Snapshot balance is serialized as string for lossless decimal reconstruction.
