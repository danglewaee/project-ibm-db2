package com.danglewaee.b2bops.inventory.domain;

import com.danglewaee.b2bops.catalog.domain.Product;
import com.danglewaee.b2bops.order.domain.SalesOrderItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "inventory_reservations")
public class InventoryReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false)
    private SalesOrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "reserved_qty", nullable = false, precision = 18, scale = 3)
    private BigDecimal reservedQty;

    @Column(name = "consumed_qty", nullable = false, precision = 18, scale = 3)
    private BigDecimal consumedQty;

    @Column(name = "released_qty", nullable = false, precision = 18, scale = 3)
    private BigDecimal releasedQty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InventoryReservationStatus status;

    @Column(name = "reserved_at", nullable = false)
    private Instant reservedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    protected InventoryReservation() {
    }

    public InventoryReservation(
            SalesOrderItem orderItem,
            Warehouse warehouse,
            Product product,
            BigDecimal reservedQty
    ) {
        this.orderItem = orderItem;
        this.warehouse = warehouse;
        this.product = product;
        this.reservedQty = reservedQty;
        this.consumedQty = BigDecimal.ZERO;
        this.releasedQty = BigDecimal.ZERO;
        this.status = InventoryReservationStatus.ACTIVE;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        reservedAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public SalesOrderItem getOrderItem() {
        return orderItem;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public Product getProduct() {
        return product;
    }

    public BigDecimal getReservedQty() {
        return reservedQty;
    }

    public InventoryReservationStatus getStatus() {
        return status;
    }

    public BigDecimal getConsumedQty() {
        return consumedQty;
    }

    public BigDecimal getReleasedQty() {
        return releasedQty;
    }

    public BigDecimal remainingQty() {
        return reservedQty.subtract(consumedQty).subtract(releasedQty);
    }

    public void release(BigDecimal quantity) {
        if (quantity.signum() <= 0) {
            throw new IllegalArgumentException("Release quantity must be greater than zero");
        }
        if (status != InventoryReservationStatus.ACTIVE) {
            throw new IllegalArgumentException("Reservation " + id + " is not active");
        }
        if (remainingQty().compareTo(quantity) < 0) {
            throw new IllegalArgumentException("Release exceeds remaining reserved quantity for reservation " + id);
        }

        releasedQty = releasedQty.add(quantity);
        if (remainingQty().signum() == 0) {
            status = consumedQty.signum() > 0
                    ? InventoryReservationStatus.CLOSED
                    : InventoryReservationStatus.CANCELLED;
            closedAt = Instant.now();
        }
    }

    public void consume(BigDecimal quantity) {
        if (quantity.signum() <= 0) {
            throw new IllegalArgumentException("Shipment quantity must be greater than zero");
        }
        if (status != InventoryReservationStatus.ACTIVE) {
            throw new IllegalArgumentException("Reservation " + id + " is not active");
        }
        if (remainingQty().compareTo(quantity) < 0) {
            throw new IllegalArgumentException("Shipment exceeds remaining reserved quantity for reservation " + id);
        }
        consumedQty = consumedQty.add(quantity);
        if (remainingQty().signum() == 0) {
            status = InventoryReservationStatus.CLOSED;
            closedAt = Instant.now();
        }
    }
}
