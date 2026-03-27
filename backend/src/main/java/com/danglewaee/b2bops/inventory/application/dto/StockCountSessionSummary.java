package com.danglewaee.b2bops.inventory.application.dto;

import com.danglewaee.b2bops.inventory.domain.StockCountItem;
import com.danglewaee.b2bops.inventory.domain.StockCountItemStatus;
import com.danglewaee.b2bops.inventory.domain.StockCountSession;
import com.danglewaee.b2bops.inventory.domain.StockCountSessionStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record StockCountSessionSummary(
        String countNumber,
        String warehouseCode,
        StockCountSessionStatus status,
        Instant startedAt,
        Instant postedAt,
        String notes,
        List<CountLineSummary> items
) {
    public static StockCountSessionSummary from(StockCountSession session) {
        return new StockCountSessionSummary(
                session.getCountNumber(),
                session.getWarehouse().getWarehouseCode(),
                session.getStatus(),
                session.getStartedAt(),
                session.getPostedAt(),
                session.getNotes(),
                session.getItems().stream()
                        .map(CountLineSummary::from)
                        .toList()
        );
    }

    public record CountLineSummary(
            Long countItemId,
            String sku,
            BigDecimal systemOnHandQty,
            BigDecimal countedOnHandQty,
            BigDecimal varianceQty,
            StockCountItemStatus status,
            String note
    ) {
        static CountLineSummary from(StockCountItem item) {
            return new CountLineSummary(
                    item.getId(),
                    item.getProduct().getSku(),
                    item.getSystemOnHandQty(),
                    item.getCountedOnHandQty(),
                    item.getVarianceQty(),
                    item.getStatus(),
                    item.getNote()
            );
        }
    }
}
