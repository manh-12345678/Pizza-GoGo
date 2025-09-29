package Group5_pizza.Pizza_GoGo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.OrderDetail;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    Optional<OrderDetail> findByOrderOrderIdAndProductProductIdAndIsDeletedFalse(Integer orderId, Integer productId);

    List<OrderDetail> findByOrderAndIsDeletedFalse(Order order);
}
