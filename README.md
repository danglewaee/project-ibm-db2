# B2B Order Allocation & Reconciliation Platform

Initial schema-first workspace for an IBM-first project built around `Db2`.

Files:
- `backend/`: Spring Boot API scaffold for the allocation and reconciliation domain.
- `docs/erd.md`: MVP entity model, relationship map, and business invariants.
- `db/db2-schema.sql`: Initial Db2 DDL for the allocation and reconciliation flow.

This scope is intentionally narrow:
- Create orders
- Reserve stock by warehouse
- Release or consume reservations
- Ship partially or fully
- Count stock and reconcile variances
- Audit important state changes

The DDL is written for a first-pass schema review, not for zero-downtime migrations.

Backend status:
- Generated from Spring Initializr and adapted for the project domain
- Exposes order creation, order lookup, and stock reservation APIs plus a system info endpoint
- Runs with `H2` in the default local profile so the project works immediately
- Seeds local customers, products, warehouses, and inventory balances for integration tests and manual API runs
- Includes a dedicated `db2` Spring profile for IBM Db2 environments
