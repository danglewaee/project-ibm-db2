package com.danglewaee.b2bops.catalog.bootstrap;

import com.danglewaee.b2bops.catalog.domain.Customer;
import com.danglewaee.b2bops.catalog.domain.Product;
import com.danglewaee.b2bops.catalog.persistence.CustomerRepository;
import com.danglewaee.b2bops.catalog.persistence.ProductRepository;
import com.danglewaee.b2bops.common.domain.RecordStatus;
import com.danglewaee.b2bops.inventory.domain.InventoryBalance;
import com.danglewaee.b2bops.inventory.domain.Warehouse;
import com.danglewaee.b2bops.inventory.persistence.InventoryBalanceRepository;
import com.danglewaee.b2bops.inventory.persistence.WarehouseRepository;
import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"local", "seed-demo-data"})
public class ReferenceDataInitializer {

    @Bean
    CommandLineRunner seedReferenceData(
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository,
            InventoryBalanceRepository inventoryBalanceRepository
    ) {
        return args -> {
            if (customerRepository.count() == 0) {
                customerRepository.save(new Customer(
                        "CUST-ACME",
                        "Acme Retail Distribution",
                        "DISTRIBUTOR",
                        RecordStatus.ACTIVE
                ));
                customerRepository.save(new Customer(
                        "CUST-NOVA",
                        "Nova Industrial Supplies",
                        "B2B",
                        RecordStatus.ACTIVE
                ));
            }

            if (productRepository.count() == 0) {
                productRepository.save(new Product("SKU-1001", "Industrial Valve", "EA", RecordStatus.ACTIVE));
                productRepository.save(new Product("SKU-1002", "Hydraulic Pump", "EA", RecordStatus.ACTIVE));
                productRepository.save(new Product("SKU-1003", "Pressure Sensor", "EA", RecordStatus.ACTIVE));
            }

            if (warehouseRepository.count() == 0) {
                warehouseRepository.save(new Warehouse(
                        "WH-EAST",
                        "East Distribution Center",
                        "New Jersey",
                        RecordStatus.ACTIVE
                ));
                warehouseRepository.save(new Warehouse(
                        "WH-SOUTH",
                        "South Distribution Center",
                        "Texas",
                        RecordStatus.ACTIVE
                ));
            }

            if (inventoryBalanceRepository.count() == 0) {
                Map<String, Product> productsBySku = productRepository.findAll().stream()
                        .collect(Collectors.toMap(Product::getSku, Function.identity()));
                Map<String, Warehouse> warehousesByCode = warehouseRepository.findAll().stream()
                        .collect(Collectors.toMap(Warehouse::getWarehouseCode, Function.identity()));

                inventoryBalanceRepository.save(new InventoryBalance(
                        warehousesByCode.get("WH-EAST"),
                        productsBySku.get("SKU-1001"),
                        new BigDecimal("25.000"),
                        BigDecimal.ZERO
                ));
                inventoryBalanceRepository.save(new InventoryBalance(
                        warehousesByCode.get("WH-EAST"),
                        productsBySku.get("SKU-1002"),
                        new BigDecimal("10.000"),
                        BigDecimal.ZERO
                ));
                inventoryBalanceRepository.save(new InventoryBalance(
                        warehousesByCode.get("WH-EAST"),
                        productsBySku.get("SKU-1003"),
                        new BigDecimal("6.000"),
                        BigDecimal.ZERO
                ));
                inventoryBalanceRepository.save(new InventoryBalance(
                        warehousesByCode.get("WH-SOUTH"),
                        productsBySku.get("SKU-1001"),
                        new BigDecimal("8.000"),
                        BigDecimal.ZERO
                ));
                inventoryBalanceRepository.save(new InventoryBalance(
                        warehousesByCode.get("WH-SOUTH"),
                        productsBySku.get("SKU-1002"),
                        new BigDecimal("14.000"),
                        BigDecimal.ZERO
                ));
                inventoryBalanceRepository.save(new InventoryBalance(
                        warehousesByCode.get("WH-SOUTH"),
                        productsBySku.get("SKU-1003"),
                        new BigDecimal("12.000"),
                        BigDecimal.ZERO
                ));
            }
        };
    }
}
