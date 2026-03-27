package com.danglewaee.b2bops.order.persistence;

import com.danglewaee.b2bops.order.domain.SalesOrder;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    @EntityGraph(attributePaths = {"customer", "lineItems", "lineItems.product"})
    Optional<SalesOrder> findByOrderNumber(String orderNumber);
}
