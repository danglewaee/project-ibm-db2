package com.danglewaee.b2bops.order.application.dto;

import com.danglewaee.b2bops.order.domain.SalesOrderItemStatus;
import com.danglewaee.b2bops.order.domain.SalesOrderStatus;
import java.math.BigDecimal;
import java.util.List;

public record ReservationSummary(
        String orderNumber,
        String warehouseCode,
        SalesOrderStatus orderStatus,
        List<ReservationLineSummary> reservations
) {
    public record ReservationLineSummary(
            Long reservationId,
            int lineNumber,
            String sku,
            BigDecimal reservedQty,
            BigDecimal itemReservedQty,
            BigDecimal availableQtyAfter,
            SalesOrderItemStatus itemStatus
    ) {
    }
}
