package com.danglewaee.b2bops.inventory.persistence;

import com.danglewaee.b2bops.inventory.domain.StockCountSession;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockCountSessionRepository extends JpaRepository<StockCountSession, Long> {

    @EntityGraph(attributePaths = {"warehouse", "items", "items.product"})
    Optional<StockCountSession> findByCountNumber(String countNumber);
}
