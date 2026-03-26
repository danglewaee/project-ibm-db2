package com.danglewaee.b2bops.order.application.dto;

import java.time.LocalDate;
import java.util.List;

public record CreateSalesOrderCommand(
        String customerCode,
        LocalDate requestedShipDate,
        int priority,
        String notes,
        List<CreateSalesOrderItemCommand> lineItems
) {
}
