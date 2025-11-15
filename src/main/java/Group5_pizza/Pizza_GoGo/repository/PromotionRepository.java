package Group5_pizza.Pizza_GoGo.repository;

import Group5_pizza.Pizza_GoGo.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    List<Promotion> findByIsDeletedFalse();
    
    List<Promotion> findByIsActiveTrueAndIsDeletedFalse();
    
    @Query("SELECT p FROM Promotion p WHERE p.isDeleted = false " +
           "AND p.isActive = true " +
           "AND (p.startDate IS NULL OR p.startDate <= :now) " +
           "AND (p.endDate IS NULL OR p.endDate >= :now) " +
           "AND (p.stockQuantity IS NULL OR p.stockQuantity > 0) " +
           "ORDER BY p.pointsRequired ASC")
    List<Promotion> findAvailablePromotions(@Param("now") LocalDateTime now);
    
    @Query("SELECT p FROM Promotion p WHERE p.type = :type " +
           "AND p.isDeleted = false AND p.isActive = true")
    List<Promotion> findByType(@Param("type") String type);
    
    Optional<Promotion> findByPromotionIdAndIsDeletedFalse(Integer promotionId);
}

