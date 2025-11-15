package Group5_pizza.Pizza_GoGo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Integer paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    private Account account;

    @Column(name = "payment_method", nullable = false, length = 20)
    @Builder.Default
    private String paymentMethod = "COD"; // COD, VNPAY, etc.

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING"; // PENDING, COMPLETED, FAILED, CANCELLED

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "transaction_id", length = 100)
    private String transactionId; // Mã giao dịch từ VNPAY hoặc hệ thống khác

    @Column(name = "notes", length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        if (paymentMethod == null) paymentMethod = "COD";
        if (amount == null) amount = BigDecimal.ZERO;
        if (status == null) status = "PENDING";
        if (isDeleted == null) isDeleted = false;
    }
}

