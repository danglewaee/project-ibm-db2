package com.danglewaee.b2bops.order.api;

import com.danglewaee.b2bops.order.domain.SalesOrderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SalesOrderResponse(
        String orderNumber,
        String customerCode,
        SalesOrderStatus status,
        int priority,
        LocalDate requestedShipDate,
        String notes,
        List<SalesOrderLineResponse> lineItems
) {
    public record SalesOrderLineResponse(
            String sku,
            BigDecimal orderedQty
    ) {
    }
}
