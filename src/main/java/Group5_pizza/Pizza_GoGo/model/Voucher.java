package Group5_pizza.Pizza_GoGo.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Vouchers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "VoucherId")
    private Integer voucherId;

    @Column(name = "Code", nullable = false, length = 100, unique = true)
    private String code;

    @Column(name = "DiscountPercent", precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(name = "DiscountAmount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "StartDate")
    private LocalDateTime startDate;

    @Column(name = "EndDate")
    private LocalDateTime endDate;

    @Column(name = "IsActive")
    private Boolean isActive;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonManagedReference
    private List<Order> orders;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}