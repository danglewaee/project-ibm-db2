package com.danglewaee.b2bops.inventory.persistence;

import com.danglewaee.b2bops.common.domain.RecordStatus;
import com.danglewaee.b2bops.inventory.domain.Warehouse;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    Optional<Warehouse> findByWarehouseCodeAndStatus(String warehouseCode, RecordStatus status);
}
