package com.danglewaee.b2bops.inventory.domain;

import com.danglewaee.b2bops.catalog.domain.Product;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "inventory_balances")
public class InventoryBalance {

    @EmbeddedId
    private InventoryBalanceId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("warehouseId")
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("productId")
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "on_hand_qty", nullable = false, precision = 18, scale = 3)
    private BigDecimal onHandQty;

    @Column(name = "reserved_qty", nullable = false, precision = 18, scale = 3)
    private BigDecimal reservedQty;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected InventoryBalance() {
    }

    public InventoryBalance(Warehouse warehouse, Product product, BigDecimal onHandQty, BigDecimal reservedQty) {
        this.id = new InventoryBalanceId(warehouse.getId(), product.getId());
        this.warehouse = warehouse;
        this.product = product;
        this.onHandQty = onHandQty;
        this.reservedQty = reservedQty;
    }

    @PrePersist
    @PreUpdate
    protected void touch() {
        updatedAt = Instant.now();
    }

    public void reserve(BigDecimal quantity) {
        if (quantity.signum() <= 0) {
            throw new IllegalArgumentException("Reservation quantity must be greater than zero");
        }
        if (availableQty().compareTo(quantity) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient available quantity for SKU " + product.getSku()
                            + " in warehouse " + warehouse.getWarehouseCode()
            );
        }
        reservedQty = reservedQty.add(quantity);
    }

    public void ship(BigDecimal quantity) {
        if (quantity.signum() <= 0) {
            throw new IllegalArgumentException("Shipment quantity must be greater than zero");
        }
        if (reservedQty.compareTo(quantity) < 0) {
            throw new IllegalArgumentException(
                    "Reserved quantity is too low to ship SKU " + product.getSku()
                            + " from warehouse " + warehouse.getWarehouseCode()
            );
        }
        if (onHandQty.compareTo(quantity) < 0) {
            throw new IllegalArgumentException(
                    "On-hand quantity is too low to ship SKU " + product.getSku()
                            + " from warehouse " + warehouse.getWarehouseCode()
            );
        }
        reservedQty = reservedQty.subtract(quantity);
        onHandQty = onHandQty.subtract(quantity);
    }

    public InventoryBalanceId getId() {
        return id;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public Product getProduct() {
        return product;
    }

    public BigDecimal getOnHandQty() {
        return onHandQty;
    }

    public BigDecimal getReservedQty() {
        return reservedQty;
    }

    public BigDecimal availableQty() {
        return onHandQty.subtract(reservedQty);
    }
}
