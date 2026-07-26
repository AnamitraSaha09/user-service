# ADR-001: User Service Design

**Status:** Accepted
**Date:** 2026-07-25
**Deciders:** Solo developer
**Parent:** ADR-000 (master architecture)

## Context

The user-service owns user identity data and is the first service built (Steps 1–2). It must integrate with Keycloak for authentication/SSO and expose profile data to other services. It also acts as a Kafka consumer in Step 5 purely to validate the event pipeline.

## Decision

- **Framework:** Spring Boot 3.x, Java 21, Maven (module of parent multi-module project).
- **Database:** PostgreSQL via Spring Data JPA + Hibernate; **Flyway** for schema migrations.
- **Auth model:** Keycloak is the *source of truth for credentials*; user-service stores only the **profile** (display name, timezone, notification email/phone) keyed by the Keycloak subject (`sub`) UUID. The service is an **OAuth2 Resource Server** validating JWTs — it never sees passwords.
- **User provisioning:** on first authenticated request, if no profile row exists for the token's `sub`, auto-create one (lazy provisioning). Simpler than Keycloak event webhooks for a learning project.
- **Kafka (Step 5 only):** a `@KafkaListener` on `task-events` that logs consumed events, proving the pipeline before notification-service exists. Can be removed later.

## Options Considered

### Option A: Store credentials in Postgres, roll your own JWT
**Pros:** full control, no external dependency.
**Cons:** re-invents auth insecurely; doesn't teach SSO; explicitly rejected by project goals.

### Option B: Keycloak owns credentials, service stores profile only — CHOSEN
| Dimension | Assessment |
|---|---|
| Complexity | Low in the service (just `spring-boot-starter-oauth2-resource-server`) |
| Security | High — no password handling |
| Learning value | High — realistic industry pattern |

**Pros:** clean separation; SSO works across task-service with the same token.
**Cons:** two sources of user data (Keycloak + profile table) that can drift; mitigated by lazy provisioning keyed on `sub`.

## Database Schema (Flyway V1)

```sql
CREATE TABLE user_profile (
    id            UUID PRIMARY KEY,            -- equals Keycloak 'sub'
    email         VARCHAR(255) NOT NULL UNIQUE,
    display_name  VARCHAR(100) NOT NULL,
    timezone      VARCHAR(50)  NOT NULL DEFAULT 'UTC',
    notify_email  BOOLEAN      NOT NULL DEFAULT TRUE,
    notify_sms    BOOLEAN      NOT NULL DEFAULT FALSE,
    phone         VARCHAR(30),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
```

## Security Configuration

- Validate JWTs against Keycloak's JWKS endpoint: `spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/taskapp`
- Keycloak realm: `taskapp`; clients: `user-service` (bearer-only), `task-service` (bearer-only), `api-tester` (public, for Postman/curl token requests).
- Endpoint rules: `/api/v1/users/me/**` requires any authenticated user; `/api/v1/users/{id}` requires the caller's `sub` to match `{id}` or role `admin`; `/actuator/health` is public.

## Consequences

- Easier: no password storage, SSO for free, other services validate the same token independently.
- Harder: local dev requires Keycloak running (add to docker-compose); tokens must be fetched for every manual test (script it).
- Revisit: Keycloak admin API integration if profile↔credential sync becomes necessary.

## Action Items

1. [ ] Scaffold module, Postgres + Flyway, profile CRUD (Step 1)
2. [ ] Build REST controllers per `user-service-openapi.yaml`: `GET`/`PUT /api/v1/users/me`, `GET /api/v1/users/{userId}`, public `GET /actuator/health`. Map `UserProfile`/`UserProfileUpdate` DTOs and the `ErrorResponse` envelope; enforce the authz rules (me = any authenticated user; `{userId}` = caller's `sub` or role `admin`). (Step 1)
3. [ ] Docker-compose Keycloak, create realm/clients, add resource-server config (Step 2)
4. [ ] Lazy provisioning filter on first authenticated request (Step 2)
5. [ ] Temporary `task-events` Kafka listener (Step 5)
