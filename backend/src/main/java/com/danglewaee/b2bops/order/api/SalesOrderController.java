package com.danglewaee.b2bops.order.api;

import com.danglewaee.b2bops.order.application.SalesOrderService;
import com.danglewaee.b2bops.order.application.dto.CreateSalesOrderCommand;
import com.danglewaee.b2bops.order.application.dto.CreateSalesOrderItemCommand;
import com.danglewaee.b2bops.order.application.dto.OrderCancellationSummary;
import com.danglewaee.b2bops.order.application.dto.ReservationSummary;
import com.danglewaee.b2bops.order.application.dto.ReserveStockCommand;
import com.danglewaee.b2bops.order.application.dto.ReserveStockLineCommand;
import com.danglewaee.b2bops.order.application.dto.SalesOrderSummary;
import com.danglewaee.b2bops.order.application.dto.ShipOrderCommand;
import com.danglewaee.b2bops.order.application.dto.ShipOrderLineCommand;
import com.danglewaee.b2bops.order.application.dto.ShipmentSummary;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
        var response = toResponse(summary);

        return ResponseEntity
                .created(URI.create("/api/v1/orders/" + summary.orderNumber()))
                .body(response);
    }

    @GetMapping("/{orderNumber}")
    public SalesOrderResponse getByOrderNumber(@PathVariable String orderNumber) {
        return toResponse(salesOrderService.getByOrderNumber(orderNumber));
    }

    @PostMapping("/{orderNumber}/reservations")
    public ResponseEntity<ReservationResponse> reserveStock(
            @PathVariable String orderNumber,
            @Valid @RequestBody CreateReservationRequest request
    ) {
        var command = new ReserveStockCommand(
                request.warehouseCode(),
                request.lineReservations().stream()
                        .map(line -> new ReserveStockLineCommand(line.sku(), line.reserveQty()))
                        .toList()
        );

        var summary = salesOrderService.reserveStock(orderNumber, command);
        return ResponseEntity.ok(toReservationResponse(summary));
    }

    @PostMapping("/{orderNumber}/shipments")
    public ResponseEntity<ShipmentResponse> shipOrder(
            @PathVariable String orderNumber,
            @Valid @RequestBody CreateShipmentRequest request
    ) {
        var command = new ShipOrderCommand(
                request.warehouseCode(),
                request.shipmentLines().stream()
                        .map(line -> new ShipOrderLineCommand(line.reservationId(), line.shipQty()))
                        .toList()
        );

        var summary = salesOrderService.shipOrder(orderNumber, command);
        return ResponseEntity.ok(toShipmentResponse(summary));
    }

    @PostMapping("/{orderNumber}/cancel")
    public ResponseEntity<OrderCancellationResponse> cancelOrder(@PathVariable String orderNumber) {
        return ResponseEntity.ok(toCancellationResponse(salesOrderService.cancelOrder(orderNumber)));
    }

    private SalesOrderResponse toResponse(SalesOrderSummary summary) {
        return new SalesOrderResponse(
                summary.orderNumber(),
                summary.customerCode(),
                summary.status(),
                summary.priority(),
                summary.requestedShipDate(),
                summary.notes(),
                summary.lineItems().stream()
                        .map(item -> new SalesOrderResponse.SalesOrderLineResponse(
                                item.lineNumber(),
                                item.sku(),
                                item.orderedQty(),
                                item.reservedQty(),
                                item.shippedQty(),
                                item.status()
                        ))
                        .toList()
        );
    }

    private ReservationResponse toReservationResponse(ReservationSummary summary) {
        return new ReservationResponse(
                summary.orderNumber(),
                summary.warehouseCode(),
                summary.orderStatus(),
                summary.reservations().stream()
                        .map(line -> new ReservationResponse.ReservationLineResponse(
                                line.reservationId(),
                                line.lineNumber(),
                                line.sku(),
                                line.reservedQty(),
                                line.itemReservedQty(),
                                line.availableQtyAfter(),
                                line.itemStatus()
                        ))
                        .toList()
        );
    }

    private ShipmentResponse toShipmentResponse(ShipmentSummary summary) {
        return new ShipmentResponse(
                summary.shipmentNumber(),
                summary.orderNumber(),
                summary.warehouseCode(),
                summary.shipmentStatus(),
                summary.orderStatus(),
                summary.shipmentLines().stream()
                        .map(line -> new ShipmentResponse.ShipmentLineResponse(
                                line.reservationId(),
                                line.lineNumber(),
                                line.sku(),
                                line.shippedQty(),
                                line.orderReservedQtyAfter(),
                                line.orderShippedQtyAfter(),
                                line.onHandQtyAfter(),
                                line.reservedQtyAfter(),
                                line.itemStatus()
                        ))
                        .toList()
        );
    }

    private OrderCancellationResponse toCancellationResponse(OrderCancellationSummary summary) {
        return new OrderCancellationResponse(
                summary.orderNumber(),
                summary.orderStatus(),
                summary.releasedReservations().stream()
                        .map(line -> new OrderCancellationResponse.ReleasedReservationResponse(
                                line.reservationId(),
                                line.lineNumber(),
                                line.warehouseCode(),
                                line.sku(),
                                line.releasedQty(),
                                line.orderReservedQtyAfter(),
                                line.availableQtyAfter(),
                                line.reservationStatus(),
                                line.itemStatus()
                        ))
                        .toList()
        );
    }
}
