package com.danglewaee.b2bops.order.application;

import com.danglewaee.b2bops.catalog.domain.Product;
import com.danglewaee.b2bops.catalog.persistence.CustomerRepository;
import com.danglewaee.b2bops.catalog.persistence.ProductRepository;
import com.danglewaee.b2bops.common.domain.RecordStatus;
import com.danglewaee.b2bops.order.application.dto.CreateSalesOrderCommand;
import com.danglewaee.b2bops.order.application.dto.ReservationSummary;
import com.danglewaee.b2bops.order.application.dto.ReserveStockCommand;
import com.danglewaee.b2bops.order.application.dto.SalesOrderSummary;
import com.danglewaee.b2bops.inventory.domain.InventoryBalanceId;
import com.danglewaee.b2bops.inventory.domain.InventoryReservation;
import com.danglewaee.b2bops.inventory.persistence.InventoryBalanceRepository;
import com.danglewaee.b2bops.inventory.persistence.InventoryReservationRepository;
import com.danglewaee.b2bops.inventory.persistence.WarehouseRepository;
import com.danglewaee.b2bops.order.domain.SalesOrder;
import com.danglewaee.b2bops.order.domain.SalesOrderItem;
import com.danglewaee.b2bops.order.domain.SalesOrderItemStatus;
import com.danglewaee.b2bops.order.domain.SalesOrderStatus;
import com.danglewaee.b2bops.order.persistence.SalesOrderRepository;
import jakarta.transaction.Transactional;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class JpaSalesOrderService implements SalesOrderService {

    private static final String CREATED_BY = "system-api";

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryBalanceRepository inventoryBalanceRepository;
    private final InventoryReservationRepository inventoryReservationRepository;

    public JpaSalesOrderService(
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            SalesOrderRepository salesOrderRepository,
            WarehouseRepository warehouseRepository,
            InventoryBalanceRepository inventoryBalanceRepository,
            InventoryReservationRepository inventoryReservationRepository
    ) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.warehouseRepository = warehouseRepository;
        this.inventoryBalanceRepository = inventoryBalanceRepository;
        this.inventoryReservationRepository = inventoryReservationRepository;
    }

    @Override
    @Transactional
    public SalesOrderSummary createDraft(CreateSalesOrderCommand command) {
        validateUniqueSkus(command);

        var customer = customerRepository
                .findByCustomerCodeAndStatus(command.customerCode(), RecordStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Active customer not found: " + command.customerCode()
                ));

        var skuToProduct = loadActiveProducts(command);

        SalesOrder order = new SalesOrder(
                "TMP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12),
                customer,
                SalesOrderStatus.DRAFT,
                (short) command.priority(),
                command.requestedShipDate(),
                command.notes(),
                CREATED_BY
        );

        for (int i = 0; i < command.lineItems().size(); i++) {
            var item = command.lineItems().get(i);
            Product product = skuToProduct.get(item.sku());
            order.addLineItem(new SalesOrderItem(
                    i + 1,
                    product,
                    item.orderedQty(),
                    SalesOrderItemStatus.OPEN,
                    null
            ));
        }

        salesOrderRepository.save(order);
        order.assignOrderNumber("SO-" + String.format("%08d", order.getId()));

        return SalesOrderSummary.from(order);
    }

    @Override
    @Transactional
    public SalesOrderSummary getByOrderNumber(String orderNumber) {
        SalesOrder order = salesOrderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));
        return SalesOrderSummary.from(order);
    }

    @Override
    @Transactional
    public ReservationSummary reserveStock(String orderNumber, ReserveStockCommand command) {
        validateUniqueReservationSkus(command);

        SalesOrder order = salesOrderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));
        validateOrderCanBeReserved(order);

        var warehouse = warehouseRepository
                .findByWarehouseCodeAndStatus(command.warehouseCode(), RecordStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Active warehouse not found: " + command.warehouseCode()
                ));

        Map<String, SalesOrderItem> itemsBySku = new LinkedHashMap<>();
        for (SalesOrderItem item : order.getLineItems()) {
            itemsBySku.put(item.getProduct().getSku(), item);
        }

        var summaries = command.lineReservations().stream().map(line -> {
            SalesOrderItem item = itemsBySku.get(line.sku());
            if (item == null) {
                throw new IllegalArgumentException(
                        "Order " + orderNumber + " does not contain SKU " + line.sku()
                );
            }

            var balanceId = new InventoryBalanceId(warehouse.getId(), item.getProduct().getId());
            var balance = inventoryBalanceRepository.findById(balanceId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Inventory balance not found for SKU " + line.sku()
                                    + " in warehouse " + warehouse.getWarehouseCode()
                    ));

            balance.reserve(line.reserveQty());
            item.reserve(line.reserveQty());

            var reservation = inventoryReservationRepository.save(new InventoryReservation(
                    item,
                    warehouse,
                    item.getProduct(),
                    line.reserveQty()
            ));

            return new ReservationSummary.ReservationLineSummary(
                    reservation.getId(),
                    item.getLineNumber(),
                    line.sku(),
                    line.reserveQty(),
                    item.getReservedQty(),
                    balance.availableQty(),
                    item.getStatus()
            );
        }).toList();

        order.refreshAllocationStatus();

        return new ReservationSummary(
                order.getOrderNumber(),
                warehouse.getWarehouseCode(),
                order.getStatus(),
                summaries
        );
    }

    private void validateUniqueSkus(CreateSalesOrderCommand command) {
        var seen = new LinkedHashSet<String>();
        var duplicates = new LinkedHashSet<String>();
        for (var item : command.lineItems()) {
            if (!seen.add(item.sku())) {
                duplicates.add(item.sku());
            }
        }
        if (!duplicates.isEmpty()) {
            throw new IllegalArgumentException("Duplicate SKU lines are not allowed: " + duplicates);
        }
    }

    private Map<String, Product> loadActiveProducts(CreateSalesOrderCommand command) {
        var skus = command.lineItems().stream().map(item -> item.sku()).toList();
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

    private void validateUniqueReservationSkus(ReserveStockCommand command) {
        var seen = new LinkedHashSet<String>();
        var duplicates = new LinkedHashSet<String>();
        for (var line : command.lineReservations()) {
            if (!seen.add(line.sku())) {
                duplicates.add(line.sku());
            }
        }
        if (!duplicates.isEmpty()) {
            throw new IllegalArgumentException("Duplicate reservation SKUs are not allowed: " + duplicates);
        }
    }

    private void validateOrderCanBeReserved(SalesOrder order) {
        if (order.getStatus() == SalesOrderStatus.CANCELLED || order.getStatus() == SalesOrderStatus.SHIPPED) {
            throw new IllegalArgumentException(
                    "Order cannot be reserved in status " + order.getStatus()
            );
        }
    }
}
