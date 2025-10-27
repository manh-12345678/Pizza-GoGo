package Group5_pizza.Pizza_GoGo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.RestaurantTable;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    Optional<Order> findByTableAndStatusAndIsDeletedFalse(RestaurantTable table, String status);

    @EntityGraph(attributePaths = { "orderDetails", "orderDetails.product" })
    Optional<Order> findByOrderIdAndIsDeletedFalse(Integer orderId);
    Optional<Order> findTopByTableOrderByCreatedAtDesc(RestaurantTable table);

    List<Order> findByStatus(String status);
}
