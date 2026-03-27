package com.danglewaee.b2bops.inventory.domain;

import com.danglewaee.b2bops.common.domain.RecordStatus;
import com.danglewaee.b2bops.common.persistence.TimestampedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "warehouses")
public class Warehouse extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "warehouse_id")
    private Long id;

    @Column(name = "warehouse_code", nullable = false, unique = true, length = 32)
    private String warehouseCode;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 200)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecordStatus status;

    protected Warehouse() {
    }

    public Warehouse(String warehouseCode, String name, String location, RecordStatus status) {
        this.warehouseCode = warehouseCode;
        this.name = name;
        this.location = location;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public RecordStatus getStatus() {
        return status;
    }
}
