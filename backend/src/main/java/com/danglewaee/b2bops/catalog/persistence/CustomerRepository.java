package com.danglewaee.b2bops.catalog.persistence;

import com.danglewaee.b2bops.catalog.domain.Customer;
import com.danglewaee.b2bops.common.domain.RecordStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCustomerCodeAndStatus(String customerCode, RecordStatus status);
}
