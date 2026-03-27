package com.danglewaee.b2bops.inventory.persistence;

import com.danglewaee.b2bops.inventory.domain.InventoryBalance;
import com.danglewaee.b2bops.inventory.domain.InventoryBalanceId;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryBalanceRepository extends JpaRepository<InventoryBalance, InventoryBalanceId> {

    @EntityGraph(attributePaths = {"warehouse", "product"})
    Optional<InventoryBalance> findById(InventoryBalanceId id);
}
