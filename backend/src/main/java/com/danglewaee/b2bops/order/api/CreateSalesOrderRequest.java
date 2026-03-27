package com.danglewaee.b2bops.order.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record CreateSalesOrderRequest(
        @NotBlank(message = "customerCode is required")
        String customerCode,
        @NotNull(message = "requestedShipDate is required")
        LocalDate requestedShipDate,
        @NotNull(message = "priority is required")
        @Min(value = 1, message = "priority must be between 1 and 5")
        @Max(value = 5, message = "priority must be between 1 and 5")
        Integer priority,
        String notes,
        @NotEmpty(message = "lineItems must not be empty")
        List<@Valid CreateSalesOrderItemRequest> lineItems
) {
}
