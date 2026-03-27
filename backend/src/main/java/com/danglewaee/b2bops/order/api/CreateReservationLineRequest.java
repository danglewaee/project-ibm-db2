package com.danglewaee.b2bops.order.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateReservationLineRequest(
        @NotBlank(message = "sku is required")
        String sku,
        @NotNull(message = "reserveQty is required")
        @DecimalMin(value = "0.001", message = "reserveQty must be greater than zero")
        BigDecimal reserveQty
) {
}
