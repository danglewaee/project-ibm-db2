package com.danglewaee.b2bops.inventory.application;

import com.danglewaee.b2bops.inventory.application.dto.CreateStockCountCommand;
import com.danglewaee.b2bops.inventory.application.dto.ReconciliationSummary;
import com.danglewaee.b2bops.inventory.application.dto.StockCountSessionSummary;

public interface StockCountService {

    StockCountSessionSummary createSession(CreateStockCountCommand command);

    StockCountSessionSummary getByCountNumber(String countNumber);

    ReconciliationSummary reconcileSession(String countNumber);
}
