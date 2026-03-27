package com.danglewaee.b2bops.order.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateShipmentLineRequest(
        @NotNull(message = "reservationId is required")
        Long reservationId,
        @NotNull(message = "shipQty is required")
        @DecimalMin(value = "0.001", message = "shipQty must be greater than zero")
        BigDecimal shipQty
) {
}
