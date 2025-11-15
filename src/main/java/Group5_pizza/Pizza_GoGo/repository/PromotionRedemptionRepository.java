package Group5_pizza.Pizza_GoGo.repository;

import Group5_pizza.Pizza_GoGo.model.Customer;
import Group5_pizza.Pizza_GoGo.model.Promotion;
import Group5_pizza.Pizza_GoGo.model.PromotionRedemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionRedemptionRepository extends JpaRepository<PromotionRedemption, Integer> {
    List<PromotionRedemption> findByCustomer(Customer customer);
    
    List<PromotionRedemption> findByCustomerOrderByCreatedAtDesc(Customer customer);
    
    List<PromotionRedemption> findByPromotion(Promotion promotion);
    
    @Query("SELECT COUNT(r) FROM PromotionRedemption r WHERE r.promotion = :promotion " +
           "AND r.status = 'COMPLETED'")
    Long countCompletedRedemptionsByPromotion(@Param("promotion") Promotion promotion);
}

