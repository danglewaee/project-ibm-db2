package com.danglewaee.b2bops.catalog.persistence;

import com.danglewaee.b2bops.catalog.domain.Product;
import com.danglewaee.b2bops.common.domain.RecordStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllBySkuInAndStatus(Collection<String> skus, RecordStatus status);
}
