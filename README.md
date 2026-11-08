# Banking System Backend

Java 17 · Spring Boot · Hibernate/JPA · MySQL · REST · JWT

## Run
Install Java 17 and Maven 3.9+, or use Docker Compose.

```sh
cp .env.example .env
# Edit .env and replace all placeholder secrets before running.
docker compose up --build
```
API: http://localhost:8080. Database is persisted in a Docker volume. To run without Docker, start a local database named `app`, export `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, then `mvn spring-boot:run`. Test with `mvn verify` (H2 test database; no external database needed).

## Authentication
```sh
curl -X POST localhost:8080/api/auth/register -H 'Content-Type: application/json' -d '{"username":"rohit","password":"change-this-password"}'
curl -X POST localhost:8080/api/auth/login -H 'Content-Type: application/json' -d '{"username":"rohit","password":"change-this-password"}'
```
Use the returned token on every protected request: `Authorization: Bearer YOUR_TOKEN`. Tokens expire after one hour. Passwords use BCrypt. Registration never accepts roles or account ownership fields.

## API
| Method | Route | Behavior |
|---|---|---|
| POST | `/api/accounts` | Create zero-balance account |
| GET | `/api/accounts` | List your accounts |
| GET | `/api/accounts/{id}` | Your account balance |
| POST | `/api/accounts/{id}/deposit` | Demo deposit `{"amount":100.00}` |
| POST | `/api/accounts/{id}/withdraw` | Withdraw `{"amount":20.00}` |
| POST | `/api/accounts/{id}/transfer` | Transfer `{"destinationId":2,"amount":30.00}` |
| GET | `/api/accounts/{id}/transactions?page=0` | Paged ledger |

Amounts use BigDecimal with two decimal places. Transfers lock both accounts in ID order, debit and credit in one database transaction, and write both ledger records atomically. All account reads and mutations verify ownership. Insufficient funds and conflicts return 409; invalid amounts return 400. Deposits represent a simulated cash-in operation for this educational project, not a verified payment integration. Clients should not automatically retry financial writes: idempotency keys are not implemented.

## Validation and scope
CI runs Maven verification on pushes and pull requests. Tests cover auth and ownership, money validation, conservation of funds, and rollback on insufficient funds.

Educational portfolio backend. Before production use, add migration-managed schemas (currently Hibernate update), rate limiting, token rotation/revocation, operational auditing, backups, and database-specific concurrency tests. Never commit `.env` or deploy example secrets. Existing starter code in the legacy subfolder is preserved for reference; the root Maven application is the supported implementation.
