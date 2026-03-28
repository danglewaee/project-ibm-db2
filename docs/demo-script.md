# Demo Script

This is a 5-minute walkthrough for interviews, portfolio reviews, or a recorded demo.

## Demo Goal

Show that the system handles an operational workflow with traceable inventory effects:

`create order -> reserve stock -> ship -> count -> reconcile -> audit`

## Setup

Use one of these:

- `local` profile for quick UI-less walkthroughs
- `Db2` validation path if you want to emphasize IBM-first data validation

Recommended setup before a live demo:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-stack-smoke.ps1 -Build
```

## Story To Tell

"The problem is not just inventory mismatch. The real problem is that operations teams can commit stock to the wrong order, fail to release reservations after cancellations, and then struggle to explain what changed after a physical stock count."

## Flow

### 1. Create an order

Explain:

- an order is created with multiple lines
- no stock is reserved yet
- the order starts as demand, not commitment

Example payload:

```json
{
  "customerCode": "CUST-ACME",
  "requestedShipDate": "2026-04-20",
  "priority": 2,
  "lineItems": [
    { "sku": "SKU-1001", "orderedQty": 4.000 },
    { "sku": "SKU-1002", "orderedQty": 1.500 }
  ]
}
```

Expected point:

- line items start open
- `reservedQty` is still `0`

### 2. Reserve stock from a warehouse

Explain:

- allocation is explicit
- reservation is warehouse-specific
- inventory availability drops because reserved quantity increases

Example payload:

```json
{
  "warehouseCode": "WH-EAST",
  "lineReservations": [
    { "sku": "SKU-1001", "reserveQty": 4.000 },
    { "sku": "SKU-1002", "reserveQty": 1.500 }
  ]
}
```

Expected point:

- order becomes `ALLOCATED`
- reservation IDs are created
- response shows `availableQtyAfter`

### 3. Ship part or all of the order

Explain:

- shipments consume reservations
- shipping affects both `on_hand` and `reserved`
- the system records a `SHIP` stock movement

Example payload:

```json
{
  "warehouseCode": "WH-EAST",
  "shipmentLines": [
    { "reservationId": 1, "shipQty": 4.000 },
    { "reservationId": 2, "shipQty": 1.500 }
  ]
}
```

Expected point:

- shipment is traceable to reservation IDs
- the order can become `PARTIALLY_SHIPPED` or `SHIPPED`

### 4. Run a cycle count

Explain:

- physical count captures what the warehouse actually saw
- reconciliation should not overwrite data silently

Example payload:

```json
{
  "warehouseCode": "WH-EAST",
  "countedBy": "warehouse.lead",
  "items": [
    { "sku": "SKU-1001", "countedOnHandQty": 23.000, "note": "2 units damaged" }
  ]
}
```

Expected point:

- the system stores system quantity and counted quantity side by side
- variance is explicit

### 5. Reconcile the count

Explain:

- reconciliation posts an adjustment movement
- the system does not hide drift; it records the correction

Expected point:

- a `COUNT_ADJUST_INCREASE` or `COUNT_ADJUST_DECREASE` movement is created
- inventory balance changes

### 6. Show the audit trail

Explain:

- operations systems need to answer "who changed what?"
- audit logs are stored as business events, not just raw framework logs

Expected point:

- show order or correlation-based audit records
- point out before/after snapshots

## Closing Lines

Use one of these endings:

- "The important design choice here is that reservation, shipment, reconciliation, and audit are all explicit state transitions."
- "I validated this workflow against live Db2 locally, so the database behavior was not left as a hand-wavy assumption."
- "The cloud deployment path is prepared for IBM Cloud Code Engine, but the value of the project is already in the transactional model and validation work."

## Portfolio Bullet Ideas

- Designed a reservation-driven order allocation backend with explicit inventory commitments and shipment consumption.
- Implemented Flyway-managed schema and validated the workflow against live Db2 in Docker.
- Modeled inventory as both current-state balances and append-only movement history for auditability.
- Added reconciliation and audit flows so operational drift and change history remain explainable.
