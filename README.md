# B2B Order Allocation & Reconciliation Platform

Initial schema-first workspace for an IBM-first project built around `Db2`.

Files:
- `backend/`: Spring Boot API scaffold for the allocation and reconciliation domain.
- `docs/erd.md`: MVP entity model, relationship map, and business invariants.
- `db/db2-schema.sql`: Initial Db2 DDL for the allocation and reconciliation flow.
- `backend/src/main/resources/db/migration/db2/V1__baseline.sql`: Flyway baseline used for IBM Db2 environments.
- `backend/src/main/resources/db/migration/h2/V1__baseline.sql`: H2-compatible baseline used for local development and tests.
- `backend/src/main/resources/db/migration/db2-demo/V2__seed_demo_data.sql`: Demo reference data for IBM Db2 smoke and demo runs.
- `backend/src/main/resources/db/migration/h2-demo/V2__seed_demo_data.sql`: Demo reference data for the default H2 local profile.
- `infra/docker-compose.db2.yml`: Local Db2 runtime for validating the `db2` Spring profile.
- `infra/docker-compose.stack.yml`: Full local stack that runs the containerized backend against Db2.
- `scripts/run-db2-smoke.ps1`: Windows helper that starts Db2 locally and runs Maven tests against the live `db2` profile.
- `scripts/run-stack-smoke.ps1`: Windows helper that builds the backend container, starts the full stack, and checks health and system info endpoints.

This scope is intentionally narrow:
- Create orders
- Reserve stock by warehouse
- Cancel orders and release reserved stock
- Release or consume reservations
- Ship partially or fully
- Count stock and reconcile variances
- Audit important state changes

The DDL is written for a first-pass schema review, not for zero-downtime migrations.

Backend status:
- Generated from Spring Initializr and adapted for the project domain
- Exposes order creation, order lookup, stock reservation, shipment, and stock count reconciliation APIs plus a system info endpoint
- Exposes order cancellation with reservation release and release stock movements
- Exposes audit trail lookup by `correlationId` or `entityType + entityId`
- Runs with `H2 + Flyway` in the default local profile so the project works immediately
- Seeds demo customers, products, warehouses, and inventory balances through Flyway migrations instead of app bootstrapping
- Writes stock movement ledger entries when reserved stock is shipped
- Reconciles cycle counts into inventory balances and writes count adjustment movements
- Persists audit logs for order, shipment, and stock count lifecycle events
- Includes a dedicated `db2` Spring profile for IBM Db2 environments
- Validates JPA mappings against the Flyway-managed schema on startup instead of generating tables with Hibernate
- Ships with a Dockerfile and full-stack Compose path for local container validation before IBM Cloud deployment

Profiles:
- Default `local`: uses `backend/src/main/resources/db/migration/h2` plus `backend/src/main/resources/db/migration/h2-demo` against in-memory `H2`
- `db2`: uses `backend/src/main/resources/db/migration/db2` against the IBM Db2 datasource from `DB2_URL`, `DB2_USERNAME`, and `DB2_PASSWORD`
- `seed-demo-data`: appends `backend/src/main/resources/db/migration/db2-demo` so the `db2` profile gets repeatable demo data without runtime seed code

Db2 smoke path:
- Start Docker Desktop and confirm `docker version` returns server info, then run `powershell -ExecutionPolicy Bypass -File .\scripts\run-db2-smoke.ps1 -StartContainer`
- Add `-RunFullSuite` to execute the current controller test suite against live Db2 instead of just the startup smoke

Full-stack container smoke path:
- Run `powershell -ExecutionPolicy Bypass -File .\scripts\run-stack-smoke.ps1 -Build`
- Add `-StopStack` if you want the script to tear the stack down after the health checks complete
- The containerized backend listens on `http://localhost:8080` and starts with `SPRING_PROFILES_ACTIVE=db2,seed-demo-data`
