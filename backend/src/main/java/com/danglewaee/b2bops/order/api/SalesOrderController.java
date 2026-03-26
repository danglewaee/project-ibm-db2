package com.danglewaee.b2bops.order.api;

import com.danglewaee.b2bops.order.application.SalesOrderService;
import com.danglewaee.b2bops.order.application.dto.CreateSalesOrderCommand;
import com.danglewaee.b2bops.order.application.dto.CreateSalesOrderItemCommand;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    public SalesOrderController(SalesOrderService salesOrderService) {
        this.salesOrderService = salesOrderService;
    }

    @PostMapping
    public ResponseEntity<SalesOrderResponse> createDraft(
            @Valid @RequestBody CreateSalesOrderRequest request
    ) {
        var command = new CreateSalesOrderCommand(
                request.customerCode(),
                request.requestedShipDate(),
                request.priority(),
                request.notes(),
                request.lineItems().stream()
                        .map(item -> new CreateSalesOrderItemCommand(
                                item.sku(),
                                item.orderedQty()
                        ))
                        .toList()
        );

        var summary = salesOrderService.createDraft(command);
        var response = new SalesOrderResponse(
                summary.orderNumber(),
                summary.customerCode(),
                summary.status(),
                summary.priority(),
                summary.requestedShipDate(),
                summary.notes(),
                summary.lineItems().stream()
                        .map(item -> new SalesOrderResponse.SalesOrderLineResponse(
                                item.sku(),
                                item.orderedQty()
                        ))
                        .toList()
        );

        return ResponseEntity
                .created(URI.create("/api/v1/orders/" + summary.orderNumber()))
                .body(response);
    }
}
