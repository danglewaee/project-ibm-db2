package com.danglewaee.b2bops.inventory.application.dto;

import com.danglewaee.b2bops.inventory.domain.StockCountItemStatus;
import com.danglewaee.b2bops.inventory.domain.StockCountSessionStatus;
import com.danglewaee.b2bops.inventory.domain.StockMovementType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ReconciliationSummary(
        String countNumber,
        String warehouseCode,
        StockCountSessionStatus countStatus,
        Instant postedAt,
        List<ReconciledLineSummary> reconciledLines
) {
    public record ReconciledLineSummary(
            Long countItemId,
            String sku,
            BigDecimal varianceQty,
            StockMovementType movementType,
            BigDecimal onHandQtyAfter,
            BigDecimal reservedQtyAfter,
            StockCountItemStatus itemStatus
    ) {
    }
}
