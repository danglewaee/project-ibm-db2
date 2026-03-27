package com.danglewaee.b2bops.inventory.api;

import com.danglewaee.b2bops.inventory.domain.StockCountItemStatus;
import com.danglewaee.b2bops.inventory.domain.StockCountSessionStatus;
import com.danglewaee.b2bops.inventory.domain.StockMovementType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ReconciliationResponse(
        String countNumber,
        String warehouseCode,
        StockCountSessionStatus countStatus,
        Instant postedAt,
        List<ReconciledLineResponse> reconciledLines
) {
    public record ReconciledLineResponse(
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
