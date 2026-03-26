package com.danglewaee.b2bops.order.application;

import com.danglewaee.b2bops.order.application.dto.CreateSalesOrderCommand;
import com.danglewaee.b2bops.order.application.dto.SalesOrderSummary;
import com.danglewaee.b2bops.order.domain.SalesOrderStatus;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class StubSalesOrderService implements SalesOrderService {

    private final AtomicLong orderSequence = new AtomicLong(10_000);

    @Override
    public SalesOrderSummary createDraft(CreateSalesOrderCommand command) {
        String orderNumber = "SO-" + orderSequence.incrementAndGet();
        var lineItems = command.lineItems().stream()
                .map(item -> new SalesOrderSummary.SalesOrderLineSummary(
                        item.sku(),
                        item.orderedQty()
                ))
                .toList();

        return new SalesOrderSummary(
                orderNumber,
                command.customerCode(),
                SalesOrderStatus.DRAFT,
                command.priority(),
                command.requestedShipDate(),
                command.notes(),
                lineItems
        );
    }
}
