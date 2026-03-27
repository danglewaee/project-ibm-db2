package com.danglewaee.b2bops.inventory.persistence;

import com.danglewaee.b2bops.inventory.domain.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {
}
