package com.danglewaee.b2bops.inventory.persistence;

import com.danglewaee.b2bops.inventory.domain.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
}
