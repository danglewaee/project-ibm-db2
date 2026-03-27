package com.danglewaee.b2bops.inventory.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateStockCountRequest(
        @NotBlank(message = "warehouseCode is required")
        String warehouseCode,
        String notes,
        @NotEmpty(message = "countItems must not be empty")
        List<@Valid CreateStockCountLineRequest> countItems
) {
}
