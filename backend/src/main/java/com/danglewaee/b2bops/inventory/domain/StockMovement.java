package com.danglewaee.b2bops.inventory.domain;

import com.danglewaee.b2bops.catalog.domain.Product;
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
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "stock_movements")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movement_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 32)
    private StockMovementType movementType;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false, length = 32)
    private StockMovementReferenceType referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "on_hand_delta", nullable = false, precision = 18, scale = 3)
    private BigDecimal onHandDelta;

    @Column(name = "reserved_delta", nullable = false, precision = 18, scale = 3)
    private BigDecimal reservedDelta;

    @Column(name = "reason_code", nullable = false, length = 64)
    private String reasonCode;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "created_by", nullable = false, length = 128)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected StockMovement() {
    }

    public StockMovement(
            Warehouse warehouse,
            Product product,
            StockMovementType movementType,
            StockMovementReferenceType referenceType,
            Long referenceId,
            BigDecimal onHandDelta,
            BigDecimal reservedDelta,
            String reasonCode,
            String note,
            String createdBy
    ) {
        this.warehouse = warehouse;
        this.product = product;
        this.movementType = movementType;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.onHandDelta = onHandDelta;
        this.reservedDelta = reservedDelta;
        this.reasonCode = reasonCode;
        this.note = note;
        this.createdBy = createdBy;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }
}
