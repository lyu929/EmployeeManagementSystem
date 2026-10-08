# Security design

## Authentication

* **Login.** `POST /api/v1/auth/login` checks the password against a BCrypt hash (cost factor 10) and
  returns an HS256-signed JWT.
  * The token is valid for 30 minutes (`app.security.token-ttl`).
  * Its claims are `sub` (username), `roles` and, for employees, `employeeId`. Issuer and expiry are
    validated on every request.
* **Identical failures.** An unknown user and a wrong password get the same response and take the same
  time: a dummy BCrypt check runs when the user does not exist.
* **Lockout.** Five failed logins for one username lock it for 15 minutes (HTTP 429 with
  `Retry-After`). The counter is kept in memory, per instance.
* **No sessions.** The API is stateless and CSRF protection is disabled. Tokens travel in the
  `Authorization` header and are never sent automatically by the browser.

## Authorization

| Path | Who |
|---|---|
| `POST /api/v1/auth/login`, `/actuator/health`, OpenAPI docs, static UI | everyone |
| `/api/v1/me/**` | any authenticated user, only their own data |
| every other `/api/v1/**` | `HR_ADMIN` (URL rule **and** `@PreAuthorize` on each controller) |
| anything else | denied |

## Sensitive data

* **SSNs.**
  * They are encrypted with AES-256-GCM, using a random IV per value; tampering is detected by the
    GCM tag.
  * A separate HMAC-SHA256 key produces a blind index. This allows exact-match search and a uniqueness
    constraint without storing the number.
  * Responses only ever contain `***-**-1234`. `GET /api/v1/employees/{id}/ssn` returns the full value,
    and every call is audited.
  * SSN search uses a POST body, so the number never appears in URLs or access logs.
* **Audit trail.** The trail covers:
  * every change to an employee, salary, login or payroll run;
  * every SSN search and every SSN view.

  Each entry records the actor, a timestamp and a summary of the change. Entries are written in the
  same transaction as the change.
* **Secrets.** All secrets come from the environment: `EMS_JWT_SECRET`, `EMS_SSN_KEY`,
  `EMS_SSN_HMAC_KEY` and the DB credentials. The application refuses to start if a key is missing or
  shorter than 32 bytes. The dev and test profiles use fixed, clearly labelled non-secret keys.
  Startup rejects the public `dev-only-` and `test-only-` keys outside those local profiles,
  rejects demo-data loading outside those profiles, and rejects combining `prod` with `dev` or `test`.
  Public demo credentials must never be reused for real accounts or real data. Historical course
  credentials in `legacy/` belong to the archived demonstration, not the maintained production service.

## HTTP

* **Error responses.** Errors are RFC 7807 problem details. They contain no stack traces or exception
  messages from unexpected errors.
* **Response headers.** Spring Security's default headers apply, plus a Content-Security-Policy that
  allows only same-origin scripts. The UI therefore uses no inline scripts or handlers, and renders all
  data with `textContent`.

## Known limitations

* Tokens cannot be revoked before they expire (no deny-list). Keep the TTL short.
* The login lockout is in memory. Multiple instances would need a shared store such as Redis.
* Rotating keys requires re-encrypting SSNs. A key-version prefix in the ciphertext would allow gradual
  rotation.
