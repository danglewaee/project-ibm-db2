package com.danglewaee.b2bops.order.application.dto;

import java.math.BigDecimal;

public record ReserveStockLineCommand(
        String sku,
        BigDecimal reserveQty
) {
}
