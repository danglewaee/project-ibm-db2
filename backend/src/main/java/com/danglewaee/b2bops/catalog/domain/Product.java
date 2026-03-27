package com.danglewaee.b2bops.catalog.domain;

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
@Table(name = "products")
public class Product extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 16)
    private String uom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecordStatus status;

    protected Product() {
    }

    public Product(String sku, String name, String uom, RecordStatus status) {
        this.sku = sku;
        this.name = name;
        this.uom = uom;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getUom() {
        return uom;
    }

    public RecordStatus getStatus() {
        return status;
    }
}
