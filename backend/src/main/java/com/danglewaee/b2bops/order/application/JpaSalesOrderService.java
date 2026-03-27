package com.danglewaee.b2bops.order.application;

import com.danglewaee.b2bops.catalog.domain.Product;
import com.danglewaee.b2bops.catalog.persistence.CustomerRepository;
import com.danglewaee.b2bops.catalog.persistence.ProductRepository;
import com.danglewaee.b2bops.common.domain.RecordStatus;
import com.danglewaee.b2bops.order.application.dto.CreateSalesOrderCommand;
import com.danglewaee.b2bops.order.application.dto.SalesOrderSummary;
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

    public JpaSalesOrderService(
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            SalesOrderRepository salesOrderRepository
    ) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.salesOrderRepository = salesOrderRepository;
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
}
