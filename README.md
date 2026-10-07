# Company Z Employee Management System

[![CI](https://github.com/lyu929/EmployeeManagementSystem/actions/workflows/ci.yml/badge.svg)](https://github.com/lyu929/EmployeeManagementSystem/actions/workflows/ci.yml)

An HR system for a small company. HR admins manage employees, salaries, payroll and reports; employees
see their own record and pay history. It began as a console application for a course group project and
was rebuilt as a secure, tested REST service with a web front end.

**Stack.** Java 17 · Spring Boot 3.3 (Web, Data JPA, Security, Validation, Actuator) · OAuth2 resource
server (JWT) · Flyway · MySQL 8 / H2 · springdoc OpenAPI · JUnit 5, MockMvc, Testcontainers · Docker ·
GitHub Actions

## Features

* **Role-based access.**
  * HR admins get employee CRUD and search by name, e-mail or exact SSN, salary changes, percentage
    raises by salary range and/or division (with a dry-run preview), payroll runs, reports, login
    management and the audit trail.
  * Employees get their profile, their pay statements and password changes.
* **Reports.**
  * Payroll totals by pay date.
  * Total pay by job title or by division for a month (employees without a job title or division
    appear as "unassigned", so the totals reconcile).
  * New hires in a date range.
* **Security.**
  * BCrypt passwords and short-lived JWTs.
  * Login lockout after repeated failures.
  * SSNs encrypted with AES-256-GCM, searchable through an HMAC blind index and masked in every
    response.
  * An audit log of changes and SSN views.
  * Secrets taken only from the environment.

  Details are in [docs/security.md](docs/security.md).
* **Data integrity.**
  * Flyway-managed schema with primary keys, foreign keys, unique and check constraints.
  * `BigDecimal` money with banker's rounding.
  * Optimistic locking on employees.
  * Transactional bulk raises (all or nothing).
* **API quality.**
  * Bean Validation with per-field error lists.
  * RFC 7807 problem responses.
  * Paging.
  * OpenAPI / Swagger UI at `/swagger-ui.html`.
  * Health probes at `/actuator/health`.
* **Web UI.** Static HTML and JavaScript served by the app, with no build step.

## Quick start

```bash
mvn spring-boot:run          # dev profile: in-memory H2 + the course data set
# open http://localhost:8080          (admin / admin123, or snoopy / emp123)
# API docs: http://localhost:8080/swagger-ui.html

mvn verify                   # unit + integration tests (the MySQL test needs Docker)
```

Production-like stack with MySQL:

```bash
cp .env.example .env         # fill in: openssl rand -base64 32 for each key, and passwords
docker compose up --build    # MySQL 8.4 + the API with the prod profile; the first admin comes from .env
```

### API at a glance

```bash
TOKEN=$(curl -s localhost:8080/api/v1/auth/login -H 'content-type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | jq -r .accessToken)

curl -s "localhost:8080/api/v1/employees?q=bun" -H "Authorization: Bearer $TOKEN"
curl -s localhost:8080/api/v1/salary-adjustments -H "Authorization: Bearer $TOKEN" \
  -H 'content-type: application/json' -d '{"percent": 3, "divisionId": 999, "dryRun": true}'
curl -s "localhost:8080/api/v1/reports/pay-by-division?month=2026-01" -H "Authorization: Bearer $TOKEN"
```

| Method and path | Role | Purpose |
|---|---|---|
| `POST /api/v1/auth/login` | – | username/password → bearer token |
| `GET /api/v1/me`, `GET /api/v1/me/pay-statements`, `PUT /api/v1/me/password` | any | self-service |
| `GET/POST /api/v1/employees`, `GET/PUT/DELETE /api/v1/employees/{id}` | HR | CRUD, paged search |
| `POST /api/v1/employees/search/ssn`, `GET /api/v1/employees/{id}/ssn` | HR | SSN lookup / reveal (audited) |
| `PUT /api/v1/employees/{id}/salary`, `POST /api/v1/salary-adjustments` | HR | salaries |
| `POST /api/v1/payroll/runs`, `GET /api/v1/employees/{id}/pay-statements` | HR | payroll |
| `GET /api/v1/reports/{payroll,pay-by-job-title,pay-by-division,new-hires}` | HR | reports |
| `GET /api/v1/divisions`, `GET /api/v1/job-titles`, `POST /api/v1/users`, `GET /api/v1/audit` | HR | administration |

Errors look like this:

```json
{"type":"about:blank","title":"Bad Request","status":400,"detail":"Request validation failed",
 "instance":"/api/v1/employees","errors":[{"field":"email","message":"must be a well-formed email address"}]}
```

## Project layout

```
src/main/java/io/github/lyu929/ems/
  config/    security (JWT, roles), OpenAPI, typed properties, key loading
  domain/    JPA entities: Employee, Division, JobTitle, PayStatement, UserAccount, AuditEntry
  repo/      Spring Data repositories and report projections (aggregations run in SQL)
  security/  SsnProtector (AES-GCM + HMAC), TokenService, LoginAttemptService, problem handlers
  service/   business logic, payroll calculator, demo-data loader, bootstrap admin
  web/       REST controllers, DTOs (records with validation), global exception handler
src/main/resources/
  db/migration/   Flyway: V1 schema, V2 reference data
  static/         web UI
src/test/java/    unit tests, MockMvc API tests, MySQL Testcontainers test
legacy/           the original console application and SQL scripts
docs/             security design, mapping from the course version
```

## Testing

* **Unit tests:**
  * SSN encryption round trip, tamper detection, blind index and masking;
  * login lockout (with a controllable clock);
  * money rounding and the payroll rates.
* **API tests:** about 20 scenarios through the real security filter chain on H2, covering:
  * authentication failures, lockout and role checks;
  * SSNs that never leak in responses;
  * validation errors and conflicts;
  * audit entries;
  * dry-run vs. applied raises;
  * idempotent payroll runs;
  * report totals.
* **MySQL compatibility:** Flyway migrations, the seed data and the report queries run on a real MySQL
  8.4 in Testcontainers, automatically skipped without Docker.

## Credits

* **Original project.** Developed as the CSC3350 Software Development group project (Spring 2026). The
  original console application and SQL scripts are preserved unchanged in [`legacy/`](legacy).
* **This version.** The Spring Boot rewrite, the security hardening and the tests are post-course work
  by Haolin Lyu. [docs/legacy_mapping.md](docs/legacy_mapping.md) maps every original menu option to
  its new endpoint and lists the issues that were fixed.
