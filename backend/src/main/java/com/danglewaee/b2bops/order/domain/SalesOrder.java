package com.danglewaee.b2bops.order.domain;

import com.danglewaee.b2bops.catalog.domain.Customer;
import com.danglewaee.b2bops.common.persistence.TimestampedEntity;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "sales_orders")
public class SalesOrder extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 32)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SalesOrderStatus status;

    @Column(nullable = false)
    private short priority;

    @Column(name = "requested_ship_date", nullable = false)
    private LocalDate requestedShipDate;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_by", nullable = false, length = 128)
    private String createdBy;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalesOrderItem> lineItems = new ArrayList<>();

    protected SalesOrder() {
    }

    public SalesOrder(
            String orderNumber,
            Customer customer,
            SalesOrderStatus status,
            short priority,
            LocalDate requestedShipDate,
            String notes,
            String createdBy
    ) {
        this.orderNumber = orderNumber;
        this.customer = customer;
        this.status = status;
        this.priority = priority;
        this.requestedShipDate = requestedShipDate;
        this.notes = notes;
        this.createdBy = createdBy;
    }

    public void addLineItem(SalesOrderItem lineItem) {
        lineItems.add(lineItem);
        lineItem.attachToOrder(this);
    }

    public void assignOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public Customer getCustomer() {
        return customer;
    }

    public SalesOrderStatus getStatus() {
        return status;
    }

    public short getPriority() {
        return priority;
    }

    public LocalDate getRequestedShipDate() {
        return requestedShipDate;
    }

    public String getNotes() {
        return notes;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public List<SalesOrderItem> getLineItems() {
        return Collections.unmodifiableList(lineItems);
    }

    public void cancel() {
        status = SalesOrderStatus.CANCELLED;
    }

    public void refreshStatus() {
        boolean allShipped = lineItems.stream()
                .allMatch(item -> item.getStatus() == SalesOrderItemStatus.SHIPPED);
        boolean anyShipped = lineItems.stream()
                .anyMatch(item -> item.getShippedQty().signum() > 0);
        boolean allAllocated = lineItems.stream()
                .allMatch(item -> item.getStatus() == SalesOrderItemStatus.ALLOCATED);
        boolean anyReserved = lineItems.stream()
                .anyMatch(item -> item.getReservedQty().signum() > 0);

        if (allShipped) {
            status = SalesOrderStatus.SHIPPED;
            return;
        }
        if (anyShipped) {
            status = SalesOrderStatus.PARTIALLY_SHIPPED;
            return;
        }
        if (allAllocated) {
            status = SalesOrderStatus.ALLOCATED;
            return;
        }
        if (anyReserved) {
            status = SalesOrderStatus.PARTIALLY_ALLOCATED;
            return;
        }
        status = SalesOrderStatus.DRAFT;
    }
}
