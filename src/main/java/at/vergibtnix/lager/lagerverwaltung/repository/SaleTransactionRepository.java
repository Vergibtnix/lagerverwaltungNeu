package at.vergibtnix.lager.lagerverwaltung.repository;

import at.vergibtnix.lager.lagerverwaltung.model.SaleTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleTransactionRepository extends JpaRepository<SaleTransaction, Long> {

    @EntityGraph(attributePaths = "product")
    List<SaleTransaction> findAllByOrderBySaleDateDescIdDesc();

    @EntityGraph(attributePaths = "product")
    List<SaleTransaction> findByProductIdOrderBySaleDateDescIdDesc(Long productId);

    @EntityGraph(attributePaths = "product")
    List<SaleTransaction> findByOwnerUsernameIgnoreCaseOrderBySaleDateDescIdDesc(String username);

    @EntityGraph(attributePaths = "product")
    List<SaleTransaction> findByProductIdAndOwnerUsernameIgnoreCaseOrderBySaleDateDescIdDesc(Long productId, String username);

    long countByProductId(Long productId);
}

