package com.danglewaee.b2bops.inventory.persistence;

import com.danglewaee.b2bops.inventory.domain.InventoryReservation;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {

    @EntityGraph(attributePaths = {"orderItem", "orderItem.order", "orderItem.product", "warehouse", "product"})
    Optional<InventoryReservation> findById(Long id);
}
