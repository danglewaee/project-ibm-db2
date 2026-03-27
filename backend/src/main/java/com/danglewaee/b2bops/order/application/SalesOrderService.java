package com.danglewaee.b2bops.order.application;

import com.danglewaee.b2bops.order.application.dto.CreateSalesOrderCommand;
import com.danglewaee.b2bops.order.application.dto.OrderCancellationSummary;
import com.danglewaee.b2bops.order.application.dto.ReservationSummary;
import com.danglewaee.b2bops.order.application.dto.ReserveStockCommand;
import com.danglewaee.b2bops.order.application.dto.SalesOrderSummary;
import com.danglewaee.b2bops.order.application.dto.ShipOrderCommand;
import com.danglewaee.b2bops.order.application.dto.ShipmentSummary;

public interface SalesOrderService {

    SalesOrderSummary createDraft(CreateSalesOrderCommand command);

    SalesOrderSummary getByOrderNumber(String orderNumber);

    ReservationSummary reserveStock(String orderNumber, ReserveStockCommand command);

    ShipmentSummary shipOrder(String orderNumber, ShipOrderCommand command);

    OrderCancellationSummary cancelOrder(String orderNumber);
}
