package com.danglewaee.b2bops.catalog.bootstrap;

import com.danglewaee.b2bops.catalog.domain.Customer;
import com.danglewaee.b2bops.catalog.domain.Product;
import com.danglewaee.b2bops.catalog.persistence.CustomerRepository;
import com.danglewaee.b2bops.catalog.persistence.ProductRepository;
import com.danglewaee.b2bops.common.domain.RecordStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class LocalReferenceDataInitializer {

    @Bean
    CommandLineRunner seedReferenceData(
            CustomerRepository customerRepository,
            ProductRepository productRepository
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
        };
    }
}
