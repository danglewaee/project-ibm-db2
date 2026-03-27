package com.danglewaee.b2bops.inventory.application.dto;

import java.math.BigDecimal;

public record CreateStockCountLineCommand(
        String sku,
        BigDecimal countedOnHandQty,
        String note
) {
}
