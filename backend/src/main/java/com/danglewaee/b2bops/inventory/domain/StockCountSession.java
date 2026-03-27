package com.danglewaee.b2bops.inventory.domain;

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
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "stock_count_sessions")
public class StockCountSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long id;

    @Column(name = "count_number", nullable = false, unique = true, length = 32)
    private String countNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StockCountSessionStatus status;

    @Column(name = "counted_by", nullable = false, length = 128)
    private String countedBy;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "posted_at")
    private Instant postedAt;

    @Column(length = 500)
    private String notes;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<StockCountItem> items = new ArrayList<>();

    protected StockCountSession() {
    }

    public StockCountSession(
            String countNumber,
            Warehouse warehouse,
            StockCountSessionStatus status,
            String countedBy,
            String notes
    ) {
        this.countNumber = countNumber;
        this.warehouse = warehouse;
        this.status = status;
        this.countedBy = countedBy;
        this.notes = notes;
    }

    @PrePersist
    protected void onCreate() {
        if (startedAt == null) {
            startedAt = Instant.now();
        }
    }

    public void addItem(StockCountItem item) {
        items.add(item);
        item.attachToSession(this);
    }

    public void assignCountNumber(String countNumber) {
        this.countNumber = countNumber;
    }

    public void markPosted() {
        if (status != StockCountSessionStatus.OPEN) {
            throw new IllegalArgumentException("Stock count session cannot be posted in status " + status);
        }
        status = StockCountSessionStatus.POSTED;
        postedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getCountNumber() {
        return countNumber;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public StockCountSessionStatus getStatus() {
        return status;
    }

    public String getCountedBy() {
        return countedBy;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getPostedAt() {
        return postedAt;
    }

    public String getNotes() {
        return notes;
    }

    public List<StockCountItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
