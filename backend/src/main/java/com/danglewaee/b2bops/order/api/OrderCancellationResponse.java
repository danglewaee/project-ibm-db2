package com.danglewaee.b2bops.order.api;

import com.danglewaee.b2bops.inventory.domain.InventoryReservationStatus;
import com.danglewaee.b2bops.order.domain.SalesOrderItemStatus;
import com.danglewaee.b2bops.order.domain.SalesOrderStatus;
import java.math.BigDecimal;
import java.util.List;

public record OrderCancellationResponse(
        String orderNumber,
        SalesOrderStatus orderStatus,
        List<ReleasedReservationResponse> releasedReservations
) {
    public record ReleasedReservationResponse(
            Long reservationId,
            int lineNumber,
            String warehouseCode,
            String sku,
            BigDecimal releasedQty,
            BigDecimal orderReservedQtyAfter,
            BigDecimal availableQtyAfter,
            InventoryReservationStatus reservationStatus,
            SalesOrderItemStatus itemStatus
    ) {
    }
}
