package com.danglewaee.b2bops.order.application;

import com.danglewaee.b2bops.order.application.dto.CreateSalesOrderCommand;
import com.danglewaee.b2bops.order.application.dto.ReservationSummary;
import com.danglewaee.b2bops.order.application.dto.ReserveStockCommand;
import com.danglewaee.b2bops.order.application.dto.SalesOrderSummary;

public interface SalesOrderService {

    SalesOrderSummary createDraft(CreateSalesOrderCommand command);

    SalesOrderSummary getByOrderNumber(String orderNumber);

    ReservationSummary reserveStock(String orderNumber, ReserveStockCommand command);
}
