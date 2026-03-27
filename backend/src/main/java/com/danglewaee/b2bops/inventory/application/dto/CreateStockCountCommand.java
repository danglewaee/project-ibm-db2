package com.danglewaee.b2bops.inventory.application.dto;

import java.util.List;

public record CreateStockCountCommand(
        String warehouseCode,
        String notes,
        List<CreateStockCountLineCommand> countItems
) {
}
