# Bank Account Management System

Spring Boot event-sourced bank account API with CQRS projections, snapshots, Docker, and PostgreSQL.

## Run with Docker

1. Copy `.env.example` to `.env` and adjust values if needed.
2. Run `docker-compose up --build`.

## API Summary

- `POST /api/accounts`
- `POST /api/accounts/{accountId}/deposit`
- `POST /api/accounts/{accountId}/withdraw`
- `POST /api/accounts/{accountId}/close`
- `GET /api/accounts/{accountId}`
- `GET /api/accounts/{accountId}/events`
- `GET /api/accounts/{accountId}/balance-at/{timestamp}`
- `GET /api/accounts/{accountId}/transactions`
- `POST /api/projections/rebuild`
- `GET /api/projections/status`
- `GET /health`

## Notes

- Events are stored immutably in the `events` table.
- Read models are updated synchronously into `account_summaries` and `transaction_history`.
- A snapshot is written after each 50th event for an account.
