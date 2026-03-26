package com.danglewaee.b2bops.order.application.dto;

import com.danglewaee.b2bops.order.domain.SalesOrderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SalesOrderSummary(
        String orderNumber,
        String customerCode,
        SalesOrderStatus status,
        int priority,
        LocalDate requestedShipDate,
        String notes,
        List<SalesOrderLineSummary> lineItems
) {
    public record SalesOrderLineSummary(
            String sku,
            BigDecimal orderedQty
    ) {
    }
}
