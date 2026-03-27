package com.danglewaee.b2bops.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class InventoryBalanceId implements Serializable {

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "product_id")
    private Long productId;

    protected InventoryBalanceId() {
    }

    public InventoryBalanceId(Long warehouseId, Long productId) {
        this.warehouseId = warehouseId;
        this.productId = productId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public Long getProductId() {
        return productId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof InventoryBalanceId that)) {
            return false;
        }
        return Objects.equals(warehouseId, that.warehouseId)
                && Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(warehouseId, productId);
    }
}
