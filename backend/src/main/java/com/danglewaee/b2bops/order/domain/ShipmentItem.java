package com.danglewaee.b2bops.order.domain;

import com.danglewaee.b2bops.catalog.domain.Product;
import com.danglewaee.b2bops.inventory.domain.InventoryReservation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "shipment_items")
public class ShipmentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipment_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private InventoryReservation reservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false)
    private SalesOrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "shipped_qty", nullable = false, precision = 18, scale = 3)
    private BigDecimal shippedQty;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ShipmentItem() {
    }

    public ShipmentItem(
            InventoryReservation reservation,
            SalesOrderItem orderItem,
            Product product,
            BigDecimal shippedQty
    ) {
        this.reservation = reservation;
        this.orderItem = orderItem;
        this.product = product;
        this.shippedQty = shippedQty;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    void attachToShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getShippedQty() {
        return shippedQty;
    }
}
