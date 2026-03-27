package com.danglewaee.b2bops.order.domain;

import com.danglewaee.b2bops.common.persistence.TimestampedEntity;
import com.danglewaee.b2bops.inventory.domain.Warehouse;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "shipments")
public class Shipment extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipment_id")
    private Long id;

    @Column(name = "shipment_number", nullable = false, unique = true, length = 32)
    private String shipmentNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private SalesOrder order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShipmentStatus status;

    @Column(name = "created_by", nullable = false, length = 128)
    private String createdBy;

    @Column(name = "shipped_at")
    private Instant shippedAt;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShipmentItem> lineItems = new ArrayList<>();

    protected Shipment() {
    }

    public Shipment(
            String shipmentNumber,
            SalesOrder order,
            Warehouse warehouse,
            ShipmentStatus status,
            String createdBy
    ) {
        this.shipmentNumber = shipmentNumber;
        this.order = order;
        this.warehouse = warehouse;
        this.status = status;
        this.createdBy = createdBy;
    }

    public void addLineItem(ShipmentItem lineItem) {
        lineItems.add(lineItem);
        lineItem.attachToShipment(this);
    }

    public void markShipped() {
        status = ShipmentStatus.SHIPPED;
        shippedAt = Instant.now();
    }

    public void assignShipmentNumber(String shipmentNumber) {
        this.shipmentNumber = shipmentNumber;
    }

    public Long getId() {
        return id;
    }

    public String getShipmentNumber() {
        return shipmentNumber;
    }

    public SalesOrder getOrder() {
        return order;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public List<ShipmentItem> getLineItems() {
        return Collections.unmodifiableList(lineItems);
    }
}
