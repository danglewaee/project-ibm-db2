package com.danglewaee.b2bops.inventory.api;

import com.danglewaee.b2bops.inventory.application.StockCountService;
import com.danglewaee.b2bops.inventory.application.dto.CreateStockCountCommand;
import com.danglewaee.b2bops.inventory.application.dto.CreateStockCountLineCommand;
import com.danglewaee.b2bops.inventory.application.dto.ReconciliationSummary;
import com.danglewaee.b2bops.inventory.application.dto.StockCountSessionSummary;
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
@RequestMapping("/api/v1/inventory/count-sessions")
public class StockCountController {

    private final StockCountService stockCountService;

    public StockCountController(StockCountService stockCountService) {
        this.stockCountService = stockCountService;
    }

    @PostMapping
    public ResponseEntity<StockCountSessionResponse> createSession(
            @Valid @RequestBody CreateStockCountRequest request
    ) {
        var command = new CreateStockCountCommand(
                request.warehouseCode(),
                request.notes(),
                request.countItems().stream()
                        .map(line -> new CreateStockCountLineCommand(
                                line.sku(),
                                line.countedOnHandQty(),
                                line.note()
                        ))
                        .toList()
        );

        var summary = stockCountService.createSession(command);
        return ResponseEntity
                .created(URI.create("/api/v1/inventory/count-sessions/" + summary.countNumber()))
                .body(toSessionResponse(summary));
    }

    @GetMapping("/{countNumber}")
    public ResponseEntity<StockCountSessionResponse> getSession(@PathVariable String countNumber) {
        return ResponseEntity.ok(toSessionResponse(stockCountService.getByCountNumber(countNumber)));
    }

    @PostMapping("/{countNumber}/reconcile")
    public ResponseEntity<ReconciliationResponse> reconcile(@PathVariable String countNumber) {
        return ResponseEntity.ok(toReconciliationResponse(stockCountService.reconcileSession(countNumber)));
    }

    private StockCountSessionResponse toSessionResponse(StockCountSessionSummary summary) {
        return new StockCountSessionResponse(
                summary.countNumber(),
                summary.warehouseCode(),
                summary.status(),
                summary.startedAt(),
                summary.postedAt(),
                summary.notes(),
                summary.items().stream()
                        .map(item -> new StockCountSessionResponse.CountItemResponse(
                                item.countItemId(),
                                item.sku(),
                                item.systemOnHandQty(),
                                item.countedOnHandQty(),
                                item.varianceQty(),
                                item.status(),
                                item.note()
                        ))
                        .toList()
        );
    }

    private ReconciliationResponse toReconciliationResponse(ReconciliationSummary summary) {
        return new ReconciliationResponse(
                summary.countNumber(),
                summary.warehouseCode(),
                summary.countStatus(),
                summary.postedAt(),
                summary.reconciledLines().stream()
                        .map(line -> new ReconciliationResponse.ReconciledLineResponse(
                                line.countItemId(),
                                line.sku(),
                                line.varianceQty(),
                                line.movementType(),
                                line.onHandQtyAfter(),
                                line.reservedQtyAfter(),
                                line.itemStatus()
                        ))
                        .toList()
        );
    }
}
