package Group5_pizza.Pizza_GoGo.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Promotions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PromotionId")
    private Integer promotionId;

    @Column(name = "Name", nullable = false, length = 200)
    private String name;

    @Column(name = "Description", length = 1000)
    private String description;

    @Column(name = "Type", nullable = false, length = 50)
    private String type; // "VOUCHER", "GIFT", "DISCOUNT"

    @Column(name = "PointsRequired", nullable = false)
    private Integer pointsRequired;

    @Column(name = "VoucherId")
    private Integer voucherId; // Nếu type là VOUCHER, link tới Voucher

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VoucherId", insertable = false, updatable = false)
    @ToString.Exclude
    @JsonManagedReference
    private Voucher voucher;

    @Column(name = "GiftName", length = 200)
    private String giftName; // Nếu type là GIFT

    @Column(name = "GiftDescription", length = 1000)
    private String giftDescription;

    @Column(name = "ProductId")
    private Integer productId; // Nếu type là PRODUCT, ID sản phẩm sẽ được tặng

    @Column(name = "ProductQuantity")
    private Integer productQuantity; // Số lượng sản phẩm tặng (mặc định 1)

    @Column(name = "ImageUrl", length = 500)
    private String imageUrl;

    @Column(name = "IsActive", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "StockQuantity")
    private Integer stockQuantity; // Số lượng còn lại (null = không giới hạn)

    @Column(name = "StartDate")
    private LocalDateTime startDate;

    @Column(name = "EndDate")
    private LocalDateTime endDate;

    @CreationTimestamp
    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "IsDeleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "promotion", fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonManagedReference
    private List<PromotionRedemption> redemptions;

    @PrePersist
    protected void onCreate() {
        if (isActive == null) isActive = true;
        if (isDeleted == null) isDeleted = false;
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isAvailable() {
        if (Boolean.FALSE.equals(isActive) || Boolean.TRUE.equals(isDeleted)) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (startDate != null && now.isBefore(startDate)) {
            return false;
        }
        if (endDate != null && now.isAfter(endDate)) {
            return false;
        }
        if (stockQuantity != null && stockQuantity <= 0) {
            return false;
        }
        return true;
    }
}

