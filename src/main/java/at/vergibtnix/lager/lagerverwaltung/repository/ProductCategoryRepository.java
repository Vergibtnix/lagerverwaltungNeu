package at.vergibtnix.lager.lagerverwaltung.repository;

import at.vergibtnix.lager.lagerverwaltung.model.ProductCategory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    Optional<ProductCategory> findByNameIgnoreCase(String name);
}

