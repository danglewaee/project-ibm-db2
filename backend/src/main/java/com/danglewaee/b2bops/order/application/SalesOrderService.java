package com.danglewaee.b2bops.order.application;

import com.danglewaee.b2bops.order.application.dto.CreateSalesOrderCommand;
import com.danglewaee.b2bops.order.application.dto.SalesOrderSummary;

public interface SalesOrderService {

    SalesOrderSummary createDraft(CreateSalesOrderCommand command);
}
