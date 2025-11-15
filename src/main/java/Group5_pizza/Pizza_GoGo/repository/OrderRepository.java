// package Group5_pizza.Pizza_GoGo.repository;
// OrderRepository.java
package Group5_pizza.Pizza_GoGo.repository;
import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.RestaurantTable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import Group5_pizza.Pizza_GoGo.DTO.DashboardRecentOrderDTO;
public interface OrderRepository extends JpaRepository<Order, Integer> {
    Optional<Order> findByTableAndStatusAndIsDeletedFalse(RestaurantTable table, String status);
    @EntityGraph(attributePaths = { "orderDetails", "orderDetails.product", "table" })
    Optional<Order> findByOrderIdAndIsDeletedFalse(Integer orderId);
    Optional<Order> findTopByTableOrderByCreatedAtDesc(RestaurantTable table);
    List<Order> findByStatus(String status);
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.orderDetails od " +
           "LEFT JOIN FETCH od.product " +
           "LEFT JOIN FETCH o.table " +
           "WHERE (:status IS NULL OR :status = '' OR o.status = :status OR o.status IS NULL) " +
           "AND o.isDeleted = false")
    List<Order> findByStatusAndIsDeletedFalse(@Param("status") String status);
    
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.orderDetails od " +
           "LEFT JOIN FETCH od.product " +
           "LEFT JOIN FETCH o.table " +
           "WHERE o.isDeleted = false")
    List<Order> findByIsDeletedFalse();
    @EntityGraph(attributePaths = {"orderDetails", "orderDetails.product", "table"})
    Optional<Order> findByTableAndStatusInAndIsDeletedFalse(RestaurantTable table, List<String> statuses);
    @EntityGraph(attributePaths = {"orderDetails", "orderDetails.product", "table"})
    Optional<Order> findTopByTableAndIsDeletedFalseOrderByCreatedAtDesc(RestaurantTable table);
    // PHƯƠNG THỨC MỚI - TÌM KIẾM CHÍNH XÁC
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderDetails od " +
            "LEFT JOIN FETCH od.product " +
            "LEFT JOIN FETCH o.table " +
            "WHERE (:orderId IS NULL OR o.orderId = :orderId) " +
            "AND (:tableNumber IS NULL OR o.table.tableNumber = :tableNumber) " +
            "AND o.isDeleted = false")
    List<Order> searchOrders(
            @Param("orderId") Integer orderId,
            @Param("tableNumber") Integer tableNumber);

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderDetails od " +
            "LEFT JOIN FETCH od.orderDetailToppings odt " + // Tải luôn toppings của detail
            "LEFT JOIN FETCH od.product " + // Tải luôn product của detail
            "WHERE o.orderId = :orderId AND o.isDeleted = false")


    List<Order> findByTableTableNumber(Integer tableNumber);

    Optional<Order> findFirstByTableTableIdOrderByCreatedAtDesc(Integer tableId);


    // SỬA LỖI LAZY (đã có từ lần trước, dùng cho 'choose_menu' và 'cart')
    // Note: Không JOIN FETCH payments vì sẽ gây MultipleBagFetchException
    // Filter các orderDetailToppings chưa bị xóa và các orderDetails chưa bị xóa
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderDetails od " +
            "LEFT JOIN FETCH od.product p " +
            "LEFT JOIN FETCH od.orderDetailToppings odt " +
            "LEFT JOIN FETCH odt.topping t " +
            "WHERE o.orderId = :orderId " +
            "AND o.isDeleted = false " +
            "AND (od.isDeleted = false OR od.isDeleted IS NULL) " +
            "AND (odt.isDeleted = false OR odt.isDeleted IS NULL)")
    Optional<Order> findByIdWithDetailsAndToppings(@Param("orderId") Integer orderId);
    
    // Load Order với OrderDetails và Product (KHÔNG JOIN FETCH toppings để tránh duplicate)
    // Sau đó sẽ load toppings riêng bằng findByOrderDetailIdWithTopping
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderDetails od " +
            "LEFT JOIN FETCH od.product p " +
            "WHERE o.orderId = :orderId " +
            "AND o.isDeleted = false " +
            "AND (od.isDeleted = false OR od.isDeleted IS NULL)")
    Optional<Order> findByIdWithDetailsOnly(@Param("orderId") Integer orderId);


    // --- THÊM CÁC HÀM MỚI ĐỂ SỬA LỖI TRANG 'manage_orders' ---

    /**
     * MỚI: Tải tất cả các đơn hàng VÀ chi tiết của chúng (orderDetails và product)
     * Dùng cho hàm getAllOrders() trong service.
     * Note: Không JOIN FETCH payments ở đây vì có thể gây duplicate results
     * Sắp xếp: PENDING trước, sau đó các trạng thái khác, và theo createdAt DESC
     */
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderDetails od " +
            "LEFT JOIN FETCH od.product p " +
            "LEFT JOIN FETCH o.table " +
            "WHERE o.isDeleted = false")
    List<Order> findAllWithDetails();

    /**
     * MỚI: Tìm đơn hàng theo số bàn VÀ chi tiết của chúng
     * Dùng cho hàm searchOrders(tableNumber) trong service.
     * Sắp xếp: PENDING trước, sau đó các trạng thái khác, và theo createdAt DESC
     */
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderDetails od " +
            "LEFT JOIN FETCH od.product p " +
            "LEFT JOIN FETCH o.table t " +
            "WHERE t.tableNumber = :tableNumber")
    List<Order> findByTableTableNumberWithDetails(@Param("tableNumber") Integer tableNumber);

    long countByIsDeletedFalse();

    long countByStatusIgnoreCaseAndIsDeletedFalse(String status);

    long countByCreatedAtBetweenAndIsDeletedFalse(LocalDateTime start, LocalDateTime end);

    @Query("SELECT new Group5_pizza.Pizza_GoGo.DTO.DashboardRecentOrderDTO(o.orderId, t.tableName, o.status, o.totalAmount, o.createdAt) " +
            "FROM Order o LEFT JOIN o.table t WHERE o.isDeleted = false ORDER BY o.createdAt DESC")
    List<DashboardRecentOrderDTO> findRecentOrderSummaries(Pageable pageable);

    List<Order> findByCreatedAtBetweenAndIsDeletedFalse(LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = {"orderDetails", "orderDetails.product", "table"})
    List<Order> findByCustomerCustomerIdAndIsDeletedFalseOrderByCreatedAtDesc(Integer customerId);

    // Lấy đơn hàng theo UserId (Account) - query trực tiếp qua accountId
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.orderDetails od " +
           "LEFT JOIN FETCH od.product p " +
           "LEFT JOIN FETCH o.table t " +
           "WHERE o.account IS NOT NULL AND o.account.userId = :userId AND o.isDeleted = false " +
           "ORDER BY o.createdAt DESC")
    List<Order> findByAccountUserIdAndIsDeletedFalseOrderByCreatedAtDesc(@Param("userId") Integer userId);

}