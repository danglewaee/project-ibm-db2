package com.danglewaee.b2bops.order.application.dto;

import com.danglewaee.b2bops.order.domain.SalesOrder;
import com.danglewaee.b2bops.order.domain.SalesOrderItemStatus;
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
    public static SalesOrderSummary from(SalesOrder order) {
        return new SalesOrderSummary(
                order.getOrderNumber(),
                order.getCustomer().getCustomerCode(),
                order.getStatus(),
                order.getPriority(),
                order.getRequestedShipDate(),
                order.getNotes(),
                order.getLineItems().stream()
                        .map(item -> new SalesOrderLineSummary(
                                item.getLineNumber(),
                                item.getProduct().getSku(),
                                item.getOrderedQty(),
                                item.getStatus()
                        ))
                        .toList()
        );
    }

    public record SalesOrderLineSummary(
            int lineNumber,
            String sku,
            BigDecimal orderedQty,
            SalesOrderItemStatus status
    ) {
    }
}
