package at.vergibtnix.lager.lagerverwaltung.repository;

import at.vergibtnix.lager.lagerverwaltung.model.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = "category")
    List<Product> findAll();

    @EntityGraph(attributePaths = "category")
    Optional<Product> findWithCategoryById(Long id);

    Optional<Product> findByNameIgnoreCase(String name);
}

