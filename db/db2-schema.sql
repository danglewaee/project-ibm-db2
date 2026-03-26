-- Initial Db2 schema for the B2B Order Allocation & Reconciliation Platform.
-- Target: Db2 LUW / Db2 on Cloud first-pass review.

CREATE SCHEMA B2BOPS;

SET CURRENT SCHEMA B2BOPS;

CREATE TABLE customers (
    customer_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY
        (START WITH 1 INCREMENT BY 1),
    customer_code VARCHAR(40) NOT NULL,
    name VARCHAR(200) NOT NULL,
    segment VARCHAR(40),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
    PRIMARY KEY (customer_id),
    CONSTRAINT uq_customers_code UNIQUE (customer_code),
    CONSTRAINT ck_customers_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE products (
    product_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY
        (START WITH 1 INCREMENT BY 1),
    sku VARCHAR(64) NOT NULL,
    name VARCHAR(200) NOT NULL,
    uom VARCHAR(16) NOT NULL DEFAULT 'EA',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
    PRIMARY KEY (product_id),
    CONSTRAINT uq_products_sku UNIQUE (sku),
    CONSTRAINT ck_products_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE warehouses (
    warehouse_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY
        (START WITH 1 INCREMENT BY 1),
    warehouse_code VARCHAR(32) NOT NULL,
    name VARCHAR(200) NOT NULL,
    location VARCHAR(200),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
    PRIMARY KEY (warehouse_id),
    CONSTRAINT uq_warehouses_code UNIQUE (warehouse_code),
    CONSTRAINT ck_warehouses_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE inventory_balances (
    warehouse_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    on_hand_qty DECIMAL(18, 3) NOT NULL DEFAULT 0,
    reserved_qty DECIMAL(18, 3) NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
    PRIMARY KEY (warehouse_id, product_id),
    CONSTRAINT fk_inventory_balances_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouses (warehouse_id),
    CONSTRAINT fk_inventory_balances_product
        FOREIGN KEY (product_id) REFERENCES products (product_id),
    CONSTRAINT ck_inventory_balances_on_hand
        CHECK (on_hand_qty >= 0),
    CONSTRAINT ck_inventory_balances_reserved
        CHECK (reserved_qty >= 0)
);

CREATE TABLE sales_orders (
    order_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY
        (START WITH 1 INCREMENT BY 1),
    order_number VARCHAR(32) NOT NULL,
    customer_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    priority SMALLINT NOT NULL DEFAULT 3,
    requested_ship_date DATE NOT NULL,
    notes VARCHAR(500),
    created_by VARCHAR(128) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
    PRIMARY KEY (order_id),
    CONSTRAINT uq_sales_orders_number UNIQUE (order_number),
    CONSTRAINT fk_sales_orders_customer
        FOREIGN KEY (customer_id) REFERENCES customers (customer_id),
    CONSTRAINT ck_sales_orders_status
        CHECK (
            status IN (
                'DRAFT',
                'CONFIRMED',
                'PARTIALLY_ALLOCATED',
                'ALLOCATED',
                'PARTIALLY_SHIPPED',
                'SHIPPED',
                'CANCELLED'
            )
        ),
    CONSTRAINT ck_sales_orders_priority
        CHECK (priority BETWEEN 1 AND 5)
);

CREATE TABLE sales_order_items (
    order_item_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY
        (START WITH 1 INCREMENT BY 1),
    order_id BIGINT NOT NULL,
    line_number INTEGER NOT NULL,
    product_id BIGINT NOT NULL,
    ordered_qty DECIMAL(18, 3) NOT NULL,
    reserved_qty DECIMAL(18, 3) NOT NULL DEFAULT 0,
    shipped_qty DECIMAL(18, 3) NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
    PRIMARY KEY (order_item_id),
    CONSTRAINT uq_sales_order_items_line UNIQUE (order_id, line_number),
    CONSTRAINT fk_sales_order_items_order
        FOREIGN KEY (order_id) REFERENCES sales_orders (order_id),
    CONSTRAINT fk_sales_order_items_product
        FOREIGN KEY (product_id) REFERENCES products (product_id),
    CONSTRAINT ck_sales_order_items_ordered
        CHECK (ordered_qty > 0),
    CONSTRAINT ck_sales_order_items_reserved
        CHECK (reserved_qty >= 0),
    CONSTRAINT ck_sales_order_items_shipped
        CHECK (shipped_qty >= 0),
    CONSTRAINT ck_sales_order_items_reserved_limit
        CHECK (reserved_qty <= ordered_qty),
    CONSTRAINT ck_sales_order_items_shipped_limit
        CHECK (shipped_qty <= ordered_qty),
    CONSTRAINT ck_sales_order_items_status
        CHECK (
            status IN (
                'OPEN',
                'PARTIALLY_ALLOCATED',
                'ALLOCATED',
                'PARTIALLY_SHIPPED',
                'SHIPPED',
                'BACKORDERED',
                'CANCELLED'
            )
        )
);

CREATE TABLE inventory_reservations (
    reservation_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY
        (START WITH 1 INCREMENT BY 1),
    order_item_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    reserved_qty DECIMAL(18, 3) NOT NULL,
    consumed_qty DECIMAL(18, 3) NOT NULL DEFAULT 0,
    released_qty DECIMAL(18, 3) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    reserved_at TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
    closed_at TIMESTAMP,
    PRIMARY KEY (reservation_id),
    CONSTRAINT fk_inventory_reservations_item
        FOREIGN KEY (order_item_id) REFERENCES sales_order_items (order_item_id),
    CONSTRAINT fk_inventory_reservations_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouses (warehouse_id),
    CONSTRAINT fk_inventory_reservations_product
        FOREIGN KEY (product_id) REFERENCES products (product_id),
    CONSTRAINT ck_inventory_reservations_qty
        CHECK (reserved_qty > 0),
    CONSTRAINT ck_inventory_reservations_consumed
        CHECK (consumed_qty >= 0),
    CONSTRAINT ck_inventory_reservations_released
        CHECK (released_qty >= 0),
    CONSTRAINT ck_inventory_reservations_balance
        CHECK (consumed_qty + released_qty <= reserved_qty),
    CONSTRAINT ck_inventory_reservations_status
        CHECK (status IN ('ACTIVE', 'CLOSED', 'CANCELLED'))
);

CREATE TABLE shipments (
    shipment_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY
        (START WITH 1 INCREMENT BY 1),
    shipment_number VARCHAR(32) NOT NULL,
    order_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by VARCHAR(128) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
    shipped_at TIMESTAMP,
    PRIMARY KEY (shipment_id),
    CONSTRAINT uq_shipments_number UNIQUE (shipment_number),
    CONSTRAINT fk_shipments_order
        FOREIGN KEY (order_id) REFERENCES sales_orders (order_id),
    CONSTRAINT fk_shipments_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouses (warehouse_id),
    CONSTRAINT ck_shipments_status
        CHECK (status IN ('DRAFT', 'SHIPPED', 'CANCELLED'))
);

CREATE TABLE shipment_items (
    shipment_item_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY
        (START WITH 1 INCREMENT BY 1),
    shipment_id BIGINT NOT NULL,
    reservation_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    shipped_qty DECIMAL(18, 3) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
    PRIMARY KEY (shipment_item_id),
    CONSTRAINT fk_shipment_items_shipment
        FOREIGN KEY (shipment_id) REFERENCES shipments (shipment_id),
    CONSTRAINT fk_shipment_items_reservation
        FOREIGN KEY (reservation_id) REFERENCES inventory_reservations (reservation_id),
    CONSTRAINT fk_shipment_items_order_item
        FOREIGN KEY (order_item_id) REFERENCES sales_order_items (order_item_id),
    CONSTRAINT fk_shipment_items_product
        FOREIGN KEY (product_id) REFERENCES products (product_id),
    CONSTRAINT ck_shipment_items_qty
        CHECK (shipped_qty > 0)
);

CREATE TABLE stock_movements (
    movement_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY
        (START WITH 1 INCREMENT BY 1),
    warehouse_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    movement_type VARCHAR(32) NOT NULL,
    reference_type VARCHAR(32) NOT NULL,
    reference_id BIGINT,
    on_hand_delta DECIMAL(18, 3) NOT NULL DEFAULT 0,
    reserved_delta DECIMAL(18, 3) NOT NULL DEFAULT 0,
    reason_code VARCHAR(64) NOT NULL,
    note VARCHAR(500),
    created_by VARCHAR(128) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
    PRIMARY KEY (movement_id),
    CONSTRAINT fk_stock_movements_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouses (warehouse_id),
    CONSTRAINT fk_stock_movements_product
        FOREIGN KEY (product_id) REFERENCES products (product_id),
    CONSTRAINT ck_stock_movements_type
        CHECK (
            movement_type IN (
                'RECEIPT',
                'RESERVE',
                'RELEASE',
                'SHIP',
                'COUNT_ADJUST_INCREASE',
                'COUNT_ADJUST_DECREASE',
                'MANUAL_ADJUSTMENT'
            )
        ),
    CONSTRAINT ck_stock_movements_reference
        CHECK (
            reference_type IN (
                'ORDER',
                'RESERVATION',
                'SHIPMENT',
                'COUNT_ITEM',
                'MANUAL'
            )
        ),
    CONSTRAINT ck_stock_movements_non_zero
        CHECK (on_hand_delta <> 0 OR reserved_delta <> 0)
);

CREATE TABLE stock_count_sessions (
    session_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY
        (START WITH 1 INCREMENT BY 1),
    count_number VARCHAR(32) NOT NULL,
    warehouse_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    counted_by VARCHAR(128) NOT NULL,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
    posted_at TIMESTAMP,
    notes VARCHAR(500),
    PRIMARY KEY (session_id),
    CONSTRAINT uq_stock_count_sessions_number UNIQUE (count_number),
    CONSTRAINT fk_stock_count_sessions_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouses (warehouse_id),
    CONSTRAINT ck_stock_count_sessions_status
        CHECK (status IN ('OPEN', 'POSTED', 'CANCELLED'))
);

CREATE TABLE stock_count_items (
    count_item_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY
        (START WITH 1 INCREMENT BY 1),
    session_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    system_on_hand_qty DECIMAL(18, 3) NOT NULL,
    counted_on_hand_qty DECIMAL(18, 3) NOT NULL,
    variance_qty DECIMAL(18, 3) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'COUNTED',
    note VARCHAR(500),
    reconciled_by VARCHAR(128),
    reconciled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
    PRIMARY KEY (count_item_id),
    CONSTRAINT uq_stock_count_items_product UNIQUE (session_id, product_id),
    CONSTRAINT fk_stock_count_items_session
        FOREIGN KEY (session_id) REFERENCES stock_count_sessions (session_id),
    CONSTRAINT fk_stock_count_items_product
        FOREIGN KEY (product_id) REFERENCES products (product_id),
    CONSTRAINT ck_stock_count_items_system
        CHECK (system_on_hand_qty >= 0),
    CONSTRAINT ck_stock_count_items_counted
        CHECK (counted_on_hand_qty >= 0),
    CONSTRAINT ck_stock_count_items_variance
        CHECK (variance_qty = counted_on_hand_qty - system_on_hand_qty),
    CONSTRAINT ck_stock_count_items_status
        CHECK (status IN ('COUNTED', 'RECONCILED', 'IGNORED'))
);

CREATE TABLE audit_logs (
    audit_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY
        (START WITH 1 INCREMENT BY 1),
    entity_type VARCHAR(32) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(32) NOT NULL,
    before_json CLOB(1M),
    after_json CLOB(1M),
    actor VARCHAR(128) NOT NULL,
    correlation_id VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
    PRIMARY KEY (audit_id),
    CONSTRAINT ck_audit_logs_action
        CHECK (
            action IN (
                'INSERT',
                'UPDATE',
                'STATUS_CHANGE',
                'RESERVE',
                'RELEASE',
                'SHIP',
                'COUNT',
                'RECONCILE',
                'CANCEL'
            )
        )
);

CREATE VIEW inventory_availability_v AS
SELECT
    warehouse_id,
    product_id,
    on_hand_qty,
    reserved_qty,
    DECIMAL(on_hand_qty - reserved_qty, 18, 3) AS available_qty,
    updated_at
FROM inventory_balances;

CREATE INDEX idx_sales_orders_status_ship_date
    ON sales_orders (status, requested_ship_date);

CREATE INDEX idx_sales_orders_customer_status
    ON sales_orders (customer_id, status);

CREATE INDEX idx_sales_order_items_order_status
    ON sales_order_items (order_id, status);

CREATE INDEX idx_sales_order_items_product_status
    ON sales_order_items (product_id, status);

CREATE INDEX idx_inventory_reservations_item_status
    ON inventory_reservations (order_item_id, status);

CREATE INDEX idx_inventory_reservations_wh_prod_status
    ON inventory_reservations (warehouse_id, product_id, status);

CREATE INDEX idx_shipments_order_status
    ON shipments (order_id, status);

CREATE INDEX idx_shipment_items_shipment
    ON shipment_items (shipment_id);

CREATE INDEX idx_stock_movements_wh_prod_created
    ON stock_movements (warehouse_id, product_id, created_at);

CREATE INDEX idx_stock_count_items_session_status
    ON stock_count_items (session_id, status);

CREATE INDEX idx_audit_logs_entity_created
    ON audit_logs (entity_type, entity_id, created_at);
