# B2B Order Allocation & Reconciliation Platform

Initial schema-first workspace for an IBM-first project built around `Db2`.

Files:
- `backend/`: Spring Boot API scaffold for the allocation and reconciliation domain.
- `docs/erd.md`: MVP entity model, relationship map, and business invariants.
- `db/db2-schema.sql`: Initial Db2 DDL for the allocation and reconciliation flow.
- `backend/src/main/resources/db/migration/db2/V1__baseline.sql`: Flyway baseline used for IBM Db2 environments.
- `backend/src/main/resources/db/migration/h2/V1__baseline.sql`: H2-compatible baseline used for local development and tests.

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
- Seeds local customers, products, warehouses, and inventory balances for integration tests and manual API runs
- Writes stock movement ledger entries when reserved stock is shipped
- Reconciles cycle counts into inventory balances and writes count adjustment movements
- Persists audit logs for order, shipment, and stock count lifecycle events
- Includes a dedicated `db2` Spring profile for IBM Db2 environments
- Validates JPA mappings against the Flyway-managed schema on startup instead of generating tables with Hibernate

Profiles:
- Default `local`: uses `backend/src/main/resources/db/migration/h2` against in-memory `H2`
- `db2`: uses `backend/src/main/resources/db/migration/db2` against the IBM Db2 datasource from `DB2_URL`, `DB2_USERNAME`, and `DB2_PASSWORD`
