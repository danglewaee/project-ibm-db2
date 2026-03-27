package com.danglewaee.b2bops.order.api;

import com.danglewaee.b2bops.order.domain.SalesOrderItemStatus;
import com.danglewaee.b2bops.order.domain.SalesOrderStatus;
import java.math.BigDecimal;
import java.util.List;

public record ReservationResponse(
        String orderNumber,
        String warehouseCode,
        SalesOrderStatus orderStatus,
        List<ReservationLineResponse> reservations
) {
    public record ReservationLineResponse(
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
