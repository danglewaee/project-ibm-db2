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
@Table(name = "stock_count_items")
public class StockCountItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "count_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private StockCountSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "system_on_hand_qty", nullable = false, precision = 18, scale = 3)
    private BigDecimal systemOnHandQty;

    @Column(name = "counted_on_hand_qty", nullable = false, precision = 18, scale = 3)
    private BigDecimal countedOnHandQty;

    @Column(name = "variance_qty", nullable = false, precision = 18, scale = 3)
    private BigDecimal varianceQty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StockCountItemStatus status;

    @Column(length = 500)
    private String note;

    @Column(name = "reconciled_by", length = 128)
    private String reconciledBy;

    @Column(name = "reconciled_at")
    private Instant reconciledAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected StockCountItem() {
    }

    public StockCountItem(
            Product product,
            BigDecimal systemOnHandQty,
            BigDecimal countedOnHandQty,
            String note
    ) {
        if (countedOnHandQty.signum() < 0) {
            throw new IllegalArgumentException("Counted on-hand quantity must be zero or greater");
        }
        this.product = product;
        this.systemOnHandQty = systemOnHandQty;
        this.countedOnHandQty = countedOnHandQty;
        this.varianceQty = countedOnHandQty.subtract(systemOnHandQty);
        this.status = StockCountItemStatus.COUNTED;
        this.note = note;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    void attachToSession(StockCountSession session) {
        this.session = session;
    }

    public void reconcile(String reconciledBy) {
        if (status != StockCountItemStatus.COUNTED) {
            throw new IllegalArgumentException("Stock count item cannot be reconciled in status " + status);
        }
        this.status = StockCountItemStatus.RECONCILED;
        this.reconciledBy = reconciledBy;
        this.reconciledAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public StockCountSession getSession() {
        return session;
    }

    public Product getProduct() {
        return product;
    }

    public BigDecimal getSystemOnHandQty() {
        return systemOnHandQty;
    }

    public BigDecimal getCountedOnHandQty() {
        return countedOnHandQty;
    }

    public BigDecimal getVarianceQty() {
        return varianceQty;
    }

    public StockCountItemStatus getStatus() {
        return status;
    }

    public String getNote() {
        return note;
    }

    public String getReconciledBy() {
        return reconciledBy;
    }

    public Instant getReconciledAt() {
        return reconciledAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
