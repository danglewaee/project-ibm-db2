package com.danglewaee.b2bops.order.api;

import com.danglewaee.b2bops.order.domain.SalesOrderItemStatus;
import com.danglewaee.b2bops.order.domain.SalesOrderStatus;
import com.danglewaee.b2bops.order.domain.ShipmentStatus;
import java.math.BigDecimal;
import java.util.List;

public record ShipmentResponse(
        String shipmentNumber,
        String orderNumber,
        String warehouseCode,
        ShipmentStatus shipmentStatus,
        SalesOrderStatus orderStatus,
        List<ShipmentLineResponse> shipmentLines
) {
    public record ShipmentLineResponse(
            Long reservationId,
            int lineNumber,
            String sku,
            BigDecimal shippedQty,
            BigDecimal orderReservedQtyAfter,
            BigDecimal orderShippedQtyAfter,
            BigDecimal onHandQtyAfter,
            BigDecimal reservedQtyAfter,
            SalesOrderItemStatus itemStatus
    ) {
    }
}
