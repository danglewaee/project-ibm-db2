package com.danglewaee.b2bops.inventory.application;

import com.danglewaee.b2bops.catalog.domain.Product;
import com.danglewaee.b2bops.catalog.persistence.ProductRepository;
import com.danglewaee.b2bops.common.domain.RecordStatus;
import com.danglewaee.b2bops.inventory.application.dto.CreateStockCountCommand;
import com.danglewaee.b2bops.inventory.application.dto.ReconciliationSummary;
import com.danglewaee.b2bops.inventory.application.dto.StockCountSessionSummary;
import com.danglewaee.b2bops.inventory.domain.InventoryBalance;
import com.danglewaee.b2bops.inventory.domain.InventoryBalanceId;
import com.danglewaee.b2bops.inventory.domain.StockCountItem;
import com.danglewaee.b2bops.inventory.domain.StockCountSession;
import com.danglewaee.b2bops.inventory.domain.StockCountSessionStatus;
import com.danglewaee.b2bops.inventory.domain.StockMovement;
import com.danglewaee.b2bops.inventory.domain.StockMovementReferenceType;
import com.danglewaee.b2bops.inventory.domain.StockMovementType;
import com.danglewaee.b2bops.inventory.domain.Warehouse;
import com.danglewaee.b2bops.inventory.persistence.InventoryBalanceRepository;
import com.danglewaee.b2bops.inventory.persistence.StockCountSessionRepository;
import com.danglewaee.b2bops.inventory.persistence.StockMovementRepository;
import com.danglewaee.b2bops.inventory.persistence.WarehouseRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class JpaStockCountService implements StockCountService {

    private static final String ACTOR = "system-api";

    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final InventoryBalanceRepository inventoryBalanceRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockCountSessionRepository stockCountSessionRepository;

    public JpaStockCountService(
            WarehouseRepository warehouseRepository,
            ProductRepository productRepository,
            InventoryBalanceRepository inventoryBalanceRepository,
            StockMovementRepository stockMovementRepository,
            StockCountSessionRepository stockCountSessionRepository
    ) {
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.inventoryBalanceRepository = inventoryBalanceRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.stockCountSessionRepository = stockCountSessionRepository;
    }

    @Override
    @Transactional
    public StockCountSessionSummary createSession(CreateStockCountCommand command) {
        validateUniqueSkus(command);

        Warehouse warehouse = warehouseRepository
                .findByWarehouseCodeAndStatus(command.warehouseCode(), RecordStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Active warehouse not found: " + command.warehouseCode()
                ));

        var skuToProduct = loadActiveProducts(command);

        StockCountSession session = stockCountSessionRepository.save(new StockCountSession(
                "TMP-CNT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10),
                warehouse,
                StockCountSessionStatus.OPEN,
                ACTOR,
                command.notes()
        ));
        session.assignCountNumber("CNT-" + String.format("%08d", session.getId()));

        for (var line : command.countItems()) {
            Product product = skuToProduct.get(line.sku());
            InventoryBalance balance = loadOrCreateBalance(warehouse, product);
            session.addItem(new StockCountItem(
                    product,
                    balance.getOnHandQty(),
                    line.countedOnHandQty(),
                    line.note()
            ));
        }

        stockCountSessionRepository.saveAndFlush(session);

        return StockCountSessionSummary.from(session);
    }

    @Override
    @Transactional
    public StockCountSessionSummary getByCountNumber(String countNumber) {
        StockCountSession session = stockCountSessionRepository.findByCountNumber(countNumber)
                .orElseThrow(() -> new StockCountSessionNotFoundException(countNumber));
        return StockCountSessionSummary.from(session);
    }

    @Override
    @Transactional
    public ReconciliationSummary reconcileSession(String countNumber) {
        StockCountSession session = stockCountSessionRepository.findByCountNumber(countNumber)
                .orElseThrow(() -> new StockCountSessionNotFoundException(countNumber));
        validateSessionCanBeReconciled(session);

        var reconciledLines = session.getItems().stream()
                .map(item -> reconcileItem(session, item))
                .toList();

        session.markPosted();

        return new ReconciliationSummary(
                session.getCountNumber(),
                session.getWarehouse().getWarehouseCode(),
                session.getStatus(),
                session.getPostedAt(),
                reconciledLines
        );
    }

    private ReconciliationSummary.ReconciledLineSummary reconcileItem(
            StockCountSession session,
            StockCountItem item
    ) {
        var balanceId = new InventoryBalanceId(session.getWarehouse().getId(), item.getProduct().getId());
        InventoryBalance balance = inventoryBalanceRepository.findById(balanceId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inventory balance not found for SKU " + item.getProduct().getSku()
                                + " in warehouse " + session.getWarehouse().getWarehouseCode()
                ));

        if (balance.getOnHandQty().compareTo(item.getSystemOnHandQty()) != 0) {
            throw new IllegalArgumentException(
                    "System on-hand quantity changed for SKU " + item.getProduct().getSku()
                            + " in warehouse " + session.getWarehouse().getWarehouseCode()
                            + " since count was captured"
            );
        }

        StockMovementType movementType = null;
        if (item.getVarianceQty().signum() > 0) {
            movementType = StockMovementType.COUNT_ADJUST_INCREASE;
        } else if (item.getVarianceQty().signum() < 0) {
            movementType = StockMovementType.COUNT_ADJUST_DECREASE;
        }

        if (movementType != null) {
            balance.adjustOnHand(item.getVarianceQty());
            stockMovementRepository.save(new StockMovement(
                    session.getWarehouse(),
                    item.getProduct(),
                    movementType,
                    StockMovementReferenceType.COUNT_ITEM,
                    item.getId(),
                    item.getVarianceQty(),
                    BigDecimal.ZERO,
                    "STOCK_COUNT_RECONCILE",
                    "Count session " + session.getCountNumber(),
                    ACTOR
            ));
        }

        item.reconcile(ACTOR);

        return new ReconciliationSummary.ReconciledLineSummary(
                item.getId(),
                item.getProduct().getSku(),
                item.getVarianceQty(),
                movementType,
                balance.getOnHandQty(),
                balance.getReservedQty(),
                item.getStatus()
        );
    }

    private InventoryBalance loadOrCreateBalance(Warehouse warehouse, Product product) {
        var balanceId = new InventoryBalanceId(warehouse.getId(), product.getId());
        return inventoryBalanceRepository.findById(balanceId)
                .orElseGet(() -> inventoryBalanceRepository.save(new InventoryBalance(
                        warehouse,
                        product,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                )));
    }

    private void validateUniqueSkus(CreateStockCountCommand command) {
        var seen = new LinkedHashSet<String>();
        var duplicates = new LinkedHashSet<String>();
        for (var line : command.countItems()) {
            if (!seen.add(line.sku())) {
                duplicates.add(line.sku());
            }
        }
        if (!duplicates.isEmpty()) {
            throw new IllegalArgumentException("Duplicate stock count SKUs are not allowed: " + duplicates);
        }
    }

    private Map<String, Product> loadActiveProducts(CreateStockCountCommand command) {
        var skus = command.countItems().stream()
                .map(line -> line.sku())
                .toList();
        var products = productRepository.findAllBySkuInAndStatus(skus, RecordStatus.ACTIVE);
        Map<String, Product> skuToProduct = new LinkedHashMap<>();
        for (Product product : products) {
            skuToProduct.put(product.getSku(), product);
        }

        var missingSkus = skus.stream()
                .filter(sku -> !skuToProduct.containsKey(sku))
                .distinct()
                .toList();
        if (!missingSkus.isEmpty()) {
            throw new IllegalArgumentException("Active products not found for SKUs: " + missingSkus);
        }

        return skuToProduct;
    }

    private void validateSessionCanBeReconciled(StockCountSession session) {
        if (session.getStatus() != StockCountSessionStatus.OPEN) {
            throw new IllegalArgumentException(
                    "Stock count session cannot be reconciled in status " + session.getStatus()
            );
        }
    }
}
