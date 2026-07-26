# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Current state vs. intended design

This repo is **design-first and largely unimplemented**. What physically exists today:

- `src/Main.java` — the default IntelliJ scaffold (a `Hello` loop). It is a placeholder, not real code, and should be removed/replaced once the Spring Boot module is scaffolded.
- `UserService.md` — **ADR-001**, the authoritative design decision record. Treat it as the source of truth for architecture.
- `user-service-openapi.yaml` — the API contract (OpenAPI 3.0.3). Endpoints and schemas should match this.

There is **no `pom.xml`, no Spring Boot code, no Flyway migrations, and no tests yet.** The build/run/test commands below do not work until the module is scaffolded. When implementing, follow the stack the ADR mandates rather than introducing alternatives.

## Intended stack (from ADR-001)

- **Spring Boot 3.x, Java 21, Maven** — this is meant to be a module of a larger multi-module parent project.
- **PostgreSQL** via Spring Data JPA + Hibernate; **Flyway** for schema migrations (first migration is `V1`, schema defined in `UserService.md`).
- **OAuth2 Resource Server** — this service never handles passwords. Keycloak (realm `taskapp`) is the source of truth for credentials; validate JWTs against its JWKS/issuer at `http://localhost:8080/realms/taskapp`.
- **Kafka** — a temporary `@KafkaListener` on topic `task-events` (Step 5 only) that just logs consumed events to validate the pipeline. Explicitly removable later.

Expected build/run/test once scaffolded (standard Spring Boot Maven, adjust if the module wraps a parent):

```bash
./mvnw spring-boot:run          # run locally (service listens on 8081 per the OpenAPI spec)
./mvnw test                     # run all tests
./mvnw test -Dtest=ClassName#method   # single test
./mvnw clean package            # build jar
```

## Core architecture concepts

These are the non-obvious design constraints that must hold across the codebase:

- **The profile's `id` IS the Keycloak `sub` (UUID).** There is no separate user id. The `user_profile.id` primary key equals the JWT subject claim. Do not generate independent user ids.
- **Lazy provisioning.** On the first authenticated request, if no `user_profile` row exists for the token's `sub`, auto-create one. There is no signup endpoint and no Keycloak webhook — provisioning happens inline. `GET /api/v1/users/me` is the canonical entry point that auto-creates.
- **This service stores profile data only** (display name, timezone, notify preferences, email/phone). Credentials, login, and password logic belong to Keycloak and must never be added here.
- **Authorization rules** (enforce in security config):
  - `/api/v1/users/me/**` — any authenticated user.
  - `/api/v1/users/{userId}` — allowed only if the caller's `sub` equals `{userId}` **or** the caller has role `admin`. This endpoint is how other services (e.g. notification-service) read a user's notify preferences.
  - `/actuator/health` — public (used by the ALB target group).
- **Two sources of user data** (Keycloak + the profile table) can drift; the mitigation is that everything is keyed on `sub`. Keep that invariant.

## API contract

`user-service-openapi.yaml` is the contract — keep controllers, DTOs, and validation in sync with it. Key points: `UserProfile` is the read model, `UserProfileUpdate` is the partial-update body for `PUT /me`, and `ErrorResponse` is the standard error envelope (`timestamp/status/error/message/path`). Local server is `http://localhost:8081`.

## Working with the design docs

When making architectural changes, update `UserService.md` (it is an ADR — append decisions rather than silently contradicting Option B, the chosen approach). The ADR's Action Items list tracks the build order: Step 1 (scaffold + Postgres/Flyway + profile CRUD), Step 2 (Keycloak docker-compose + resource-server config + lazy-provisioning filter), Step 5 (temporary Kafka listener).
