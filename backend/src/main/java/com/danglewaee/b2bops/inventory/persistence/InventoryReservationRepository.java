package com.danglewaee.b2bops.inventory.persistence;

import com.danglewaee.b2bops.inventory.domain.InventoryReservation;
import com.danglewaee.b2bops.inventory.domain.InventoryReservationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {

    @EntityGraph(attributePaths = {"orderItem", "orderItem.order", "orderItem.product", "warehouse", "product"})
    Optional<InventoryReservation> findById(Long id);

    @EntityGraph(attributePaths = {"orderItem", "orderItem.order", "orderItem.product", "warehouse", "product"})
    List<InventoryReservation> findAllByOrderItem_Order_IdAndStatus(
            Long orderId,
            InventoryReservationStatus status
    );
}
