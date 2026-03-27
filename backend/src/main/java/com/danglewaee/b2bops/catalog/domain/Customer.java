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
@Table(name = "customers")
public class Customer extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long id;

    @Column(name = "customer_code", nullable = false, unique = true, length = 40)
    private String customerCode;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 40)
    private String segment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecordStatus status;

    protected Customer() {
    }

    public Customer(String customerCode, String name, String segment, RecordStatus status) {
        this.customerCode = customerCode;
        this.name = name;
        this.segment = segment;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public String getName() {
        return name;
    }

    public String getSegment() {
        return segment;
    }

    public RecordStatus getStatus() {
        return status;
    }
}
