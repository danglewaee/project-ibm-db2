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
- Exposes a draft order creation API and system info endpoint
- Keeps Db2 wiring in configuration, but temporarily runs in stub mode until persistence is implemented
