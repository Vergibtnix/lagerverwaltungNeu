package at.vergibtnix.lager.lagerverwaltung.repository;

import at.vergibtnix.lager.lagerverwaltung.model.RestockOrder;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestockOrderRepository extends JpaRepository<RestockOrder, Long> {

    @EntityGraph(attributePaths = "product")
    List<RestockOrder> findAllByOrderByOrderedDateDescIdDesc();

    @EntityGraph(attributePaths = "product")
    List<RestockOrder> findByProductIdOrderByOrderedDateDescIdDesc(Long productId);
}

