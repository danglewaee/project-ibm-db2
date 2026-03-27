package com.danglewaee.b2bops.order.domain;

import com.danglewaee.b2bops.catalog.domain.Product;
import com.danglewaee.b2bops.common.persistence.TimestampedEntity;
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
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "sales_order_items")
public class SalesOrderItem extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private SalesOrder order;

    @Column(name = "line_number", nullable = false)
    private int lineNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "ordered_qty", nullable = false, precision = 18, scale = 3)
    private BigDecimal orderedQty;

    @Column(name = "reserved_qty", nullable = false, precision = 18, scale = 3)
    private BigDecimal reservedQty;

    @Column(name = "shipped_qty", nullable = false, precision = 18, scale = 3)
    private BigDecimal shippedQty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SalesOrderItemStatus status;

    @Column(length = 500)
    private String notes;

    protected SalesOrderItem() {
    }

    public SalesOrderItem(
            int lineNumber,
            Product product,
            BigDecimal orderedQty,
            SalesOrderItemStatus status,
            String notes
    ) {
        this.lineNumber = lineNumber;
        this.product = product;
        this.orderedQty = orderedQty;
        this.reservedQty = BigDecimal.ZERO;
        this.shippedQty = BigDecimal.ZERO;
        this.status = status;
        this.notes = notes;
    }

    void attachToOrder(SalesOrder order) {
        this.order = order;
    }

    public Long getId() {
        return id;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public SalesOrder getOrder() {
        return order;
    }

    public Product getProduct() {
        return product;
    }

    public BigDecimal getOrderedQty() {
        return orderedQty;
    }

    public BigDecimal getReservedQty() {
        return reservedQty;
    }

    public BigDecimal getShippedQty() {
        return shippedQty;
    }

    public SalesOrderItemStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public BigDecimal remainingToReserve() {
        return orderedQty.subtract(reservedQty);
    }

    public void reserve(BigDecimal quantity) {
        if (quantity.signum() <= 0) {
            throw new IllegalArgumentException("Reservation quantity must be greater than zero");
        }
        if (remainingToReserve().compareTo(quantity) < 0) {
            throw new IllegalArgumentException(
                    "Reservation exceeds remaining quantity for SKU " + product.getSku()
            );
        }
        reservedQty = reservedQty.add(quantity);
        refreshStatusFromQuantities();
    }

    public BigDecimal remainingToShip() {
        return orderedQty.subtract(shippedQty);
    }

    public void ship(BigDecimal quantity) {
        if (quantity.signum() <= 0) {
            throw new IllegalArgumentException("Shipment quantity must be greater than zero");
        }
        if (reservedQty.compareTo(quantity) < 0) {
            throw new IllegalArgumentException(
                    "Shipment exceeds reserved quantity for SKU " + product.getSku()
            );
        }
        if (remainingToShip().compareTo(quantity) < 0) {
            throw new IllegalArgumentException(
                    "Shipment exceeds remaining order quantity for SKU " + product.getSku()
            );
        }
        reservedQty = reservedQty.subtract(quantity);
        shippedQty = shippedQty.add(quantity);
        refreshStatusFromQuantities();
    }

    public void release(BigDecimal quantity) {
        if (quantity.signum() <= 0) {
            throw new IllegalArgumentException("Release quantity must be greater than zero");
        }
        if (reservedQty.compareTo(quantity) < 0) {
            throw new IllegalArgumentException(
                    "Release exceeds reserved quantity for SKU " + product.getSku()
            );
        }
        reservedQty = reservedQty.subtract(quantity);
        refreshStatusFromQuantities();
    }

    public void cancelOutstandingDemand() {
        if (shippedQty.compareTo(orderedQty) == 0) {
            status = SalesOrderItemStatus.SHIPPED;
            return;
        }
        if (shippedQty.signum() > 0) {
            status = SalesOrderItemStatus.PARTIALLY_SHIPPED;
            return;
        }
        status = SalesOrderItemStatus.CANCELLED;
    }

    private void refreshStatusFromQuantities() {
        if (shippedQty.compareTo(orderedQty) == 0) {
            status = SalesOrderItemStatus.SHIPPED;
            return;
        }
        if (shippedQty.signum() > 0) {
            status = SalesOrderItemStatus.PARTIALLY_SHIPPED;
            return;
        }
        if (reservedQty.compareTo(orderedQty) == 0) {
            status = SalesOrderItemStatus.ALLOCATED;
            return;
        }
        if (reservedQty.signum() > 0) {
            status = SalesOrderItemStatus.PARTIALLY_ALLOCATED;
            return;
        }
        status = SalesOrderItemStatus.OPEN;
    }
}
