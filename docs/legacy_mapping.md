# From the console project to the REST service

The original CSC3350 group project is preserved in [`legacy/`](../legacy). Every feature it had is
still available, now behind an HTTP API.

| Course feature (console menu) | Now |
|---|---|
| Login, role check in the menu | `POST /api/v1/auth/login` → JWT; roles are enforced per URL **and** per method (`@PreAuthorize`) |
| Search by empID / name / SSN | `GET /api/v1/employees/{id}`, `GET /api/v1/employees?q=` (paged), `POST /api/v1/employees/search/ssn` |
| Add / update / delete employee | `POST`, `PUT`, `DELETE /api/v1/employees/{id}` with validation and an audit record |
| Update one salary | `PUT /api/v1/employees/{id}/salary` |
| Raise by salary range / by division | `POST /api/v1/salary-adjustments` (both filters can be combined; `dryRun` previews) |
| Update division / job title | part of `PUT /api/v1/employees/{id}` |
| Payroll summary by pay date | `GET /api/v1/reports/payroll?payDate=` |
| Total pay by job title / division | `GET /api/v1/reports/pay-by-job-title?month=`, `.../pay-by-division?month=` |
| New hires by date range | `GET /api/v1/reports/new-hires?from=&to=` |
| Employee: view own info / pay history | `GET /api/v1/me`, `GET /api/v1/me/pay-statements` |
| — | new: payroll runs, password change, login management, audit trail, OpenAPI docs, web UI |

## Problems fixed

| Original | Risk | Now |
|---|---|---|
| `DriverManager` with `root` and an empty password hard-coded in `DBConnection` | full DB access for anyone with the code | HikariCP pool; credentials from environment variables; dedicated DB user in `docker-compose.yml` |
| Passwords stored and compared in plain text (`WHERE password = ?`) | a DB leak exposes every password | BCrypt hashes; constant-time failure path; lockout after 5 failures |
| SSNs stored in clear text and searchable with `WHERE SSN = ?` | a DB leak exposes SSNs | AES-256-GCM encryption, HMAC blind index for search, masked everywhere, full value only via an audited endpoint |
| `double` for salaries | rounding errors in money | `BigDecimal` with two decimals and banker's rounding |
| `payroll`, `job_titles`, `division` without primary keys; no foreign keys; `SET FOREIGN_KEY_CHECKS=0` in the seed | orphans and duplicates | Flyway-managed schema with primary keys, foreign keys, unique and check constraints |
| Manual multi-step deletes without a transaction | partially deleted employees | one transaction; `ON DELETE CASCADE` for pay statements and the login |
| `catch (Exception e) { println(...) }` | silent failures | typed exceptions mapped to RFC 7807 problem responses; unexpected errors logged |
| Seed data: two pay rows computed from another employee's salary (`payID` 2 and 8) | wrong demo reports | pay statements computed from each employee's own salary |
| No tests | — | unit tests, MockMvc integration tests (H2) and a MySQL 8.4 Testcontainers test in CI |
