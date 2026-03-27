INSERT INTO customers (customer_code, name, segment, status)
SELECT 'CUST-ACME', 'Acme Retail Distribution', 'DISTRIBUTOR', 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM customers
    WHERE customer_code = 'CUST-ACME'
);

INSERT INTO customers (customer_code, name, segment, status)
SELECT 'CUST-NOVA', 'Nova Industrial Supplies', 'B2B', 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM customers
    WHERE customer_code = 'CUST-NOVA'
);

INSERT INTO products (sku, name, uom, status)
SELECT 'SKU-1001', 'Industrial Valve', 'EA', 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE sku = 'SKU-1001'
);

INSERT INTO products (sku, name, uom, status)
SELECT 'SKU-1002', 'Hydraulic Pump', 'EA', 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE sku = 'SKU-1002'
);

INSERT INTO products (sku, name, uom, status)
SELECT 'SKU-1003', 'Pressure Sensor', 'EA', 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM products
    WHERE sku = 'SKU-1003'
);

INSERT INTO warehouses (warehouse_code, name, location, status)
SELECT 'WH-EAST', 'East Distribution Center', 'New Jersey', 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM warehouses
    WHERE warehouse_code = 'WH-EAST'
);

INSERT INTO warehouses (warehouse_code, name, location, status)
SELECT 'WH-SOUTH', 'South Distribution Center', 'Texas', 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM warehouses
    WHERE warehouse_code = 'WH-SOUTH'
);

INSERT INTO inventory_balances (warehouse_id, product_id, on_hand_qty, reserved_qty)
SELECT
    (SELECT warehouse_id FROM warehouses WHERE warehouse_code = 'WH-EAST'),
    (SELECT product_id FROM products WHERE sku = 'SKU-1001'),
    25.000,
    0.000
WHERE NOT EXISTS (
    SELECT 1
    FROM inventory_balances
    WHERE warehouse_id = (SELECT warehouse_id FROM warehouses WHERE warehouse_code = 'WH-EAST')
      AND product_id = (SELECT product_id FROM products WHERE sku = 'SKU-1001')
);

INSERT INTO inventory_balances (warehouse_id, product_id, on_hand_qty, reserved_qty)
SELECT
    (SELECT warehouse_id FROM warehouses WHERE warehouse_code = 'WH-EAST'),
    (SELECT product_id FROM products WHERE sku = 'SKU-1002'),
    10.000,
    0.000
WHERE NOT EXISTS (
    SELECT 1
    FROM inventory_balances
    WHERE warehouse_id = (SELECT warehouse_id FROM warehouses WHERE warehouse_code = 'WH-EAST')
      AND product_id = (SELECT product_id FROM products WHERE sku = 'SKU-1002')
);

INSERT INTO inventory_balances (warehouse_id, product_id, on_hand_qty, reserved_qty)
SELECT
    (SELECT warehouse_id FROM warehouses WHERE warehouse_code = 'WH-EAST'),
    (SELECT product_id FROM products WHERE sku = 'SKU-1003'),
    6.000,
    0.000
WHERE NOT EXISTS (
    SELECT 1
    FROM inventory_balances
    WHERE warehouse_id = (SELECT warehouse_id FROM warehouses WHERE warehouse_code = 'WH-EAST')
      AND product_id = (SELECT product_id FROM products WHERE sku = 'SKU-1003')
);

INSERT INTO inventory_balances (warehouse_id, product_id, on_hand_qty, reserved_qty)
SELECT
    (SELECT warehouse_id FROM warehouses WHERE warehouse_code = 'WH-SOUTH'),
    (SELECT product_id FROM products WHERE sku = 'SKU-1001'),
    8.000,
    0.000
WHERE NOT EXISTS (
    SELECT 1
    FROM inventory_balances
    WHERE warehouse_id = (SELECT warehouse_id FROM warehouses WHERE warehouse_code = 'WH-SOUTH')
      AND product_id = (SELECT product_id FROM products WHERE sku = 'SKU-1001')
);

INSERT INTO inventory_balances (warehouse_id, product_id, on_hand_qty, reserved_qty)
SELECT
    (SELECT warehouse_id FROM warehouses WHERE warehouse_code = 'WH-SOUTH'),
    (SELECT product_id FROM products WHERE sku = 'SKU-1002'),
    14.000,
    0.000
WHERE NOT EXISTS (
    SELECT 1
    FROM inventory_balances
    WHERE warehouse_id = (SELECT warehouse_id FROM warehouses WHERE warehouse_code = 'WH-SOUTH')
      AND product_id = (SELECT product_id FROM products WHERE sku = 'SKU-1002')
);

INSERT INTO inventory_balances (warehouse_id, product_id, on_hand_qty, reserved_qty)
SELECT
    (SELECT warehouse_id FROM warehouses WHERE warehouse_code = 'WH-SOUTH'),
    (SELECT product_id FROM products WHERE sku = 'SKU-1003'),
    12.000,
    0.000
WHERE NOT EXISTS (
    SELECT 1
    FROM inventory_balances
    WHERE warehouse_id = (SELECT warehouse_id FROM warehouses WHERE warehouse_code = 'WH-SOUTH')
      AND product_id = (SELECT product_id FROM products WHERE sku = 'SKU-1003')
);
