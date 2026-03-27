package com.danglewaee.b2bops.order.application.dto;

import java.math.BigDecimal;

public record CreateSalesOrderItemCommand(
        String sku,
        BigDecimal orderedQty
) {
}
