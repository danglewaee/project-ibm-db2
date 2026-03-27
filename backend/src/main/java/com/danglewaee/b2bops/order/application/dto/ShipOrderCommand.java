package com.danglewaee.b2bops.order.application.dto;

import java.util.List;

public record ShipOrderCommand(
        String warehouseCode,
        List<ShipOrderLineCommand> shipmentLines
) {
}
