package Group5_pizza.Pizza_GoGo.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import Group5_pizza.Pizza_GoGo.DTO.DashboardRecentPaymentDTO;
import Group5_pizza.Pizza_GoGo.model.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    
    // Tìm tất cả payment chưa xóa
    List<Payment> findByIsDeletedFalse();
    
    // Tìm payment theo orderId
    List<Payment> findByOrderOrderIdAndIsDeletedFalse(Integer orderId);
    
    // Tìm payment theo accountId
    List<Payment> findByAccountUserIdAndIsDeletedFalse(Integer accountId);
    
    // Tìm payment theo status
    List<Payment> findByStatusAndIsDeletedFalse(String status);
    
    // Tìm payment theo paymentMethod
    List<Payment> findByPaymentMethodAndIsDeletedFalse(String paymentMethod);
    
    // Tìm payment theo orderId và status
    List<Payment> findByOrderOrderIdAndStatusAndIsDeletedFalse(Integer orderId, String status);
    
    // Tìm payment đang pending cho một order
    @Query("SELECT p FROM Payment p WHERE p.order.orderId = :orderId AND p.status = 'PENDING' AND p.isDeleted = false")
    List<Payment> findPendingPaymentsByOrderId(@Param("orderId") Integer orderId);
    
    // Tìm payment đã completed cho một order
    @Query("SELECT p FROM Payment p WHERE p.order.orderId = :orderId AND p.status = 'COMPLETED' AND p.isDeleted = false")
    List<Payment> findCompletedPaymentsByOrderId(@Param("orderId") Integer orderId);
    
    // Tìm payment theo transactionId
    Optional<Payment> findByTransactionIdAndIsDeletedFalse(String transactionId);

    long countByIsDeletedFalse();

    long countByStatusAndIsDeletedFalse(String status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'COMPLETED' AND p.isDeleted = false")
    BigDecimal sumCompletedPayments();

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'COMPLETED' AND p.isDeleted = false AND p.paymentDate BETWEEN :start AND :end")
    BigDecimal sumCompletedPaymentsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT new Group5_pizza.Pizza_GoGo.DTO.DashboardRecentPaymentDTO(p.paymentId, p.order.orderId, p.paymentMethod, p.status, p.amount, p.createdAt) " +
            "FROM Payment p WHERE p.isDeleted = false ORDER BY p.createdAt DESC")
    List<DashboardRecentPaymentDTO> findRecentPaymentSummaries(Pageable pageable);
}

