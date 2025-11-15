// package Group5_pizza.Pizza_GoGo.repository;
// OrderDetailRepository.java
package Group5_pizza.Pizza_GoGo.repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.OrderDetail;
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    Optional<OrderDetail> findByOrderOrderIdAndProductProductIdAndIsDeletedFalse(Integer orderId, Integer productId);
    List<OrderDetail> findByOrderAndIsDeletedFalse(Order order);
    
    @EntityGraph(attributePaths = {"orderDetailToppings", "orderDetailToppings.topping"})
    @Query("SELECT od FROM OrderDetail od WHERE od.orderDetailId = :orderDetailId")
    Optional<OrderDetail> findByIdWithToppings(@Param("orderDetailId") Integer orderDetailId);
}