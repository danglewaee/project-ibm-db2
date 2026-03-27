package com.danglewaee.b2bops.inventory.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateStockCountLineRequest(
        @NotBlank(message = "sku is required")
        String sku,
        @NotNull(message = "countedOnHandQty is required")
        @DecimalMin(value = "0.000", message = "countedOnHandQty must be zero or greater")
        BigDecimal countedOnHandQty,
        String note
) {
}
