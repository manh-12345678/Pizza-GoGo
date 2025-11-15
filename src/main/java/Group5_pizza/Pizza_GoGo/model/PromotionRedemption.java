package Group5_pizza.Pizza_GoGo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "PromotionRedemptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionRedemption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RedemptionId")
    private Integer redemptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerId", nullable = false)
    @ToString.Exclude
    @JsonBackReference
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PromotionId", nullable = false)
    @ToString.Exclude
    @JsonBackReference
    private Promotion promotion;

    @Column(name = "PointsUsed", nullable = false)
    private Integer pointsUsed;

    @Column(name = "Status", nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING"; // PENDING, COMPLETED, CANCELLED

    @Column(name = "VoucherCode", length = 100)
    private String voucherCode; // Mã voucher được tạo nếu type là VOUCHER

    @Column(name = "Notes", length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "CompletedAt")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        if (status == null) status = "PENDING";
    }
}

