package com.danglewaee.b2bops.inventory.api;

import com.danglewaee.b2bops.inventory.domain.StockCountItemStatus;
import com.danglewaee.b2bops.inventory.domain.StockCountSessionStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record StockCountSessionResponse(
        String countNumber,
        String warehouseCode,
        StockCountSessionStatus status,
        Instant startedAt,
        Instant postedAt,
        String notes,
        List<CountItemResponse> items
) {
    public record CountItemResponse(
            Long countItemId,
            String sku,
            BigDecimal systemOnHandQty,
            BigDecimal countedOnHandQty,
            BigDecimal varianceQty,
            StockCountItemStatus status,
            String note
    ) {
    }
}
