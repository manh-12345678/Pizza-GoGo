// package Group5_pizza.Pizza_GoGo.repository;
// OrderDetailToppingRepository.java
package Group5_pizza.Pizza_GoGo.repository;
import Group5_pizza.Pizza_GoGo.model.OrderDetailTopping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface OrderDetailToppingRepository extends JpaRepository<OrderDetailTopping, Integer> {
    // Kiểm tra xem topping đã được thêm vào order detail chưa (chưa bị xóa)
    // Trả về List vì có thể có nhiều bản ghi (do lỗi dữ liệu hoặc logic cũ)
    @Query("SELECT odt FROM OrderDetailTopping odt WHERE odt.orderDetail.orderDetailId = :orderDetailId " +
           "AND odt.topping.toppingId = :toppingId AND (odt.isDeleted = false OR odt.isDeleted IS NULL)")
    List<OrderDetailTopping> findByOrderDetailIdAndToppingIdAndNotDeleted(
            @Param("orderDetailId") Integer orderDetailId,
            @Param("toppingId") Integer toppingId);
    
    // Load tất cả toppings của một orderDetail (chưa bị xóa) với topping được JOIN FETCH
    // Sử dụng native query để đảm bảo query chạy đúng với column trực tiếp
    @Query(value = "SELECT odt.* FROM order_detail_toppings odt " +
           "LEFT JOIN toppings t ON t.topping_id = odt.topping_id " +
           "WHERE odt.order_detail_id = :orderDetailId " +
           "AND (odt.is_deleted = 0 OR odt.is_deleted IS NULL) " +
           "ORDER BY odt.order_detail_topping_id", nativeQuery = true)
    List<OrderDetailTopping> findByOrderDetailIdWithToppingNative(@Param("orderDetailId") Integer orderDetailId);
    
    // Load tất cả toppings của một orderDetail (chưa bị xóa) với topping được JOIN FETCH
    // Sử dụng JPQL với relationship
    @Query("SELECT odt FROM OrderDetailTopping odt " +
           "LEFT JOIN FETCH odt.topping t " +
           "WHERE odt.orderDetail.orderDetailId = :orderDetailId " +
           "AND (odt.isDeleted = false OR odt.isDeleted IS NULL) " +
           "ORDER BY odt.orderDetailToppingId")
    List<OrderDetailTopping> findByOrderDetailIdWithTopping(@Param("orderDetailId") Integer orderDetailId);
}