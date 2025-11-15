package Group5_pizza.Pizza_GoGo.repository;

import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.Shipping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingRepository extends JpaRepository<Shipping, Integer> {
    List<Shipping> findByOrder(Order order);
    
    Optional<Shipping> findByOrderOrderId(Integer orderId);
    
    List<Shipping> findByStatus(String status);
    
    @Query("SELECT s FROM Shipping s WHERE s.status = :status ORDER BY s.createdAt DESC")
    List<Shipping> findByStatusOrderByCreatedAtDesc(@Param("status") String status);
    
    @Query("SELECT s FROM Shipping s WHERE s.order.orderId = :orderId")
    Optional<Shipping> findShippingByOrderId(@Param("orderId") Integer orderId);
    
    @Query("SELECT s FROM Shipping s WHERE s.order.orderId = :orderId")
    List<Shipping> findShippingsByOrderId(@Param("orderId") Integer orderId);
}

