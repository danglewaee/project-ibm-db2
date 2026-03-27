package com.danglewaee.b2bops.order.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateReservationRequest(
        @NotBlank(message = "warehouseCode is required")
        String warehouseCode,
        @NotEmpty(message = "lineReservations must not be empty")
        List<@Valid CreateReservationLineRequest> lineReservations
) {
}
