package com.danglewaee.b2bops.order.persistence;

import com.danglewaee.b2bops.order.domain.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
}
