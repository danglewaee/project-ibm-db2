# B2B Order Allocation & Reconciliation Platform

An IBM-first backend project for a distributor-style operations workflow:

`sales order -> reserve stock -> ship partially or fully -> count stock -> reconcile variance -> audit the lifecycle`

The core business problem is not "inventory mismatch" by itself. The real problem is that operations teams can promise the wrong delivery date, over-allocate the same SKU across multiple orders, and struggle to explain what changed after cycle counts or cancellations.

## Why This Project Matters

This project is meaningful because it proves enterprise-style backend work, not because it has a cloud logo on the README.

- It models a transactional workflow where order, reservation, shipment, and reconciliation states must stay consistent.
- It keeps both current-state inventory and an append-only movement ledger, which is the kind of design tradeoff that actually matters in operational systems.
- It persists audit trails for high-risk state changes instead of treating observability as an afterthought.
- It validates the same schema and business flows against live `Db2`, not just an in-memory dev database.

What is true today:

- The business workflow is implemented end to end.
- `Db2` migrations and application mappings were validated against a live `Db2` container.
- The backend is containerized and has an `IBM Cloud Code Engine` deployment path.

What is not claimed:

- This repository does not claim a live paid `IBM Cloud + Db2` production deployment unless that deployment is actually created in your account.

That line matters. It keeps the project honest.

## Problem Statement

The system targets three operational roles:

- `Sales Ops`: creates and updates customer orders.
- `Warehouse Ops`: reserves stock, ships stock, and performs cycle counts.
- `Operations Manager`: reviews shortages, reconciliation drift, and audit history.

The system is designed to answer questions like:

- Which warehouse reserved this SKU for which order line?
- What stock is still available to promise?
- What changed after a cancellation or cycle count?
- Which user-facing order commitments are now at risk because inventory changed?

## Technical Scope

This scope is intentionally narrow and defensible:

- Create orders
- Reserve stock by warehouse
- Cancel orders and release reserved stock
- Ship partially or fully
- Count stock and reconcile variances
- Record audit events for important state changes

The DDL is written for a first-pass schema review and developer validation flow, not for zero-downtime migration guarantees.

## Evidence Of Depth

- `inventory_balances` stores current operational state
- `stock_movements` stores an append-only inventory ledger
- `inventory_reservations` makes allocation explicit and traceable
- `shipment_items` ties physical shipment consumption back to a reservation
- `audit_logs` records before/after state for key business transitions

This gives the project real engineering weight even before a paid cloud deployment exists.

## Repository Map

- `backend/`: Spring Boot API for the allocation and reconciliation domain
- `docs/architecture.md`: system context, runtime profiles, and honest deployment status
- `docs/demo-script.md`: 5-minute walkthrough for portfolio demos and interviews
- `docs/erd.md`: MVP entity model, relationship map, and business invariants
- `db/db2-schema.sql`: initial Db2-first schema draft
- `backend/src/main/resources/db/migration/db2/V1__baseline.sql`: Flyway baseline for IBM Db2
- `backend/src/main/resources/db/migration/h2/V1__baseline.sql`: H2-compatible baseline for local development and tests
- `backend/src/main/resources/db/migration/db2-demo/V2__seed_demo_data.sql`: demo reference data for IBM Db2 validation
- `backend/src/main/resources/db/migration/h2-demo/V2__seed_demo_data.sql`: demo reference data for the default local profile
- `infra/docker-compose.db2.yml`: local Db2 runtime for validating the `db2` profile
- `infra/docker-compose.stack.yml`: full local stack for backend + Db2
- `infra/codeengine/db2-secret.env.template`: template for the Db2 secret consumed by IBM Cloud Code Engine
- `scripts/run-db2-smoke.ps1`: start Db2 locally and run tests against live Db2
- `scripts/run-stack-smoke.ps1`: build the containerized backend and verify the full local stack
- `scripts/deploy-code-engine.ps1`: idempotent IBM Cloud Code Engine deployment helper

## Current Backend Status

- Exposes order creation, order lookup, reservation, shipment, cancellation, stock count, reconciliation, and audit lookup APIs
- Runs immediately with `H2 + Flyway` in the default local profile
- Seeds demo customers, products, warehouses, and inventory balances through Flyway migrations
- Includes a dedicated `db2` profile for IBM Db2 validation
- Validates JPA mappings against Flyway-managed schema instead of generating tables with Hibernate
- Ships with a Dockerfile and a full-stack Compose path for local container validation before cloud deployment

## Runtime Profiles

- Default `local`: uses `backend/src/main/resources/db/migration/h2` plus `backend/src/main/resources/db/migration/h2-demo` against in-memory `H2`
- `db2`: uses `backend/src/main/resources/db/migration/db2` against the IBM Db2 datasource from `DB2_URL`, `DB2_USERNAME`, and `DB2_PASSWORD`
- `seed-demo-data`: appends `backend/src/main/resources/db/migration/db2-demo` so the `db2` profile gets repeatable demo data without runtime seed code

## Validation Paths

Local `Db2` validation:

- Start Docker Desktop and confirm `docker version` returns server info
- Run `powershell -ExecutionPolicy Bypass -File .\scripts\run-db2-smoke.ps1 -StartContainer`
- Add `-RunFullSuite` to execute the controller suite against live Db2 instead of just startup smoke

Full-stack container validation:

- Run `powershell -ExecutionPolicy Bypass -File .\scripts\run-stack-smoke.ps1 -Build`
- Add `-StopStack` if you want the script to tear the stack down after health checks complete
- The containerized backend listens on `http://localhost:8080` and starts with `SPRING_PROFILES_ACTIVE=db2,seed-demo-data`

IBM Cloud Code Engine deployment path:

- Install `IBM Cloud CLI` and the `code-engine` plugin, or reuse the path already configured by `scripts/deploy-code-engine.ps1`
- Copy `infra\codeengine\db2-secret.env.template` to `infra\codeengine\db2-secret.env` and fill in the reachable Db2 connection values
- If you are not already logged in, run `powershell -ExecutionPolicy Bypass -File .\scripts\deploy-code-engine.ps1 -UseSso` or pass `-ApiKeyFile`
- The script creates or selects the Code Engine project, creates or updates the generic secret, and creates or updates the application from the local `backend` Dockerfile source
- If your shell cannot resolve `ibmcloud`, pass `-IBMCloudExecutable` with the full path to `ibmcloud.exe`
- Use `-DryRun` first if you want to inspect the exact `ibmcloud ce` commands before they run

## Interview Or Portfolio Framing

Use this repo honestly:

- "Built an IBM-first B2B allocation platform with Spring Boot, Flyway, Docker, and Db2-validated workflows."
- "Modeled inventory allocation explicitly through reservations, shipment consumption, reconciliation, and append-only stock movements."
- "Prepared an IBM Cloud Code Engine deployment path, while validating the data layer and business flows against live Db2 in Docker."

That is a stronger and more defensible story than claiming a cloud deployment you did not actually run.
