package com.danglewaee.b2bops.order.application.dto;

import java.util.List;

public record ReserveStockCommand(
        String warehouseCode,
        List<ReserveStockLineCommand> lineReservations
) {
}
