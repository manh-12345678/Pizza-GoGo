package Group5_pizza.Pizza_GoGo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph; // <-- Thêm import
import org.springframework.data.jpa.repository.JpaRepository;

import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.RestaurantTable;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    Optional<Order> findByTableAndStatusAndIsDeletedFalse(RestaurantTable table, String status);

    // Thêm "table" vào EntityGraph để lấy số bàn
    @EntityGraph(attributePaths = { "orderDetails", "orderDetails.product", "table" })
    Optional<Order> findByOrderIdAndIsDeletedFalse(Integer orderId);

    Optional<Order> findTopByTableOrderByCreatedAtDesc(RestaurantTable table);

    // Phương thức cũ (có thể giữ lại nếu dùng ở chỗ khác)
    List<Order> findByStatus(String status);

    // THÊM MỚI: Tải đầy đủ chi tiết theo Status (cho Admin)
    @EntityGraph(attributePaths = {"orderDetails", "orderDetails.product", "table"})
    List<Order> findByStatusAndIsDeletedFalse(String status);

    // Tải đầy đủ chi tiết khi filter là "all" (cho Admin)
    @EntityGraph(attributePaths = {"orderDetails", "orderDetails.product", "table"})
    List<Order> findByIsDeletedFalse();
}