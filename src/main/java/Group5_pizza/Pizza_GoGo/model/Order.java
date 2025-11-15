// package Group5_pizza.Pizza_GoGo.model;
// Order.java
package Group5_pizza.Pizza_GoGo.model;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.ColumnDefault; // <-- THÊM IMPORT NÀY

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "Orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderId")
    private Integer orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerId")
    @JsonIgnore // Ngăn vòng lặp
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AccountId", nullable = true) // Cho phép null để guest có thể thanh toán
    @JsonIgnore
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TableId")
    @JsonIgnore
    private RestaurantTable table;

    @Column(name = "OrderType", nullable = false, length = 20)
    private String orderType;

    // --- SỬA LỖI DDL TẠI ĐÂY ---
    // Bỏ columnDefinition và dùng precision/scale chuẩn
    @Column(name = "TotalAmount", nullable = false, precision = 18, scale = 2)
    @ColumnDefault("0.00") // Dùng annotation này để tương thích với 'validate'
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "DepositAmount", nullable = false, precision = 18, scale = 2)
    @ColumnDefault("0.00")
    @Builder.Default
    private BigDecimal depositAmount = BigDecimal.ZERO;

    @Column(name = "PaidAmount", nullable = false, precision = 18, scale = 2)
    @ColumnDefault("0.00")
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;
    // --- KẾT THÚC SỬA LỖI ---

    @Column(name = "Status", nullable = false, length = 20, columnDefinition = "NVARCHAR(20) DEFAULT 'Pending'")
    @Builder.Default
    private String status = "Pending";

    @CreationTimestamp
    @Column(name = "CreatedAt", updatable = false, columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    @Column(name = "IsDeleted", columnDefinition = "BIT DEFAULT 0")
    @Builder.Default
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // Cho phép serialize danh sách này
    @Builder.Default
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @OneToMany(mappedBy = "order")
    @JsonIgnore // Không cần trả về frontend
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "order")
    @JsonIgnore
    @Builder.Default
    private List<Shipping> shippings = new ArrayList<>();

    @OneToMany(mappedBy = "order")
    @JsonIgnore
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VoucherId")
    @JsonIgnore
    private Voucher voucher;

    // ==================== HELPER METHODS ====================
    public void addOrderDetail(OrderDetail detail) {
        if (orderDetails == null) orderDetails = new ArrayList<>();
        orderDetails.add(detail);
        detail.setOrder(this);
        recalculateTotal();
    }
    public void removeOrderDetail(OrderDetail detail) {
        if (orderDetails != null) {
            orderDetails.remove(detail);
            detail.setOrder(null);
        }
        recalculateTotal();
    }
    public void clearOrderDetails() {
        if (orderDetails != null) {
            orderDetails.clear();
        }
        recalculateTotal();
    }
    public void recalculateTotal() {
        BigDecimal total = orderDetails.stream()
                .filter(d -> !Boolean.TRUE.equals(d.getIsDeleted()))
                .map(OrderDetail::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (voucher != null) {
            BigDecimal voucherDiscount = BigDecimal.ZERO;
            if (voucher.getDiscountPercent() != null
                    && voucher.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
                voucherDiscount = voucherDiscount.add(
                        total.multiply(voucher.getDiscountPercent())
                                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
            }
            if (voucher.getDiscountAmount() != null
                    && voucher.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                voucherDiscount = voucherDiscount.add(voucher.getDiscountAmount());
            }
            if (voucherDiscount.compareTo(total) > 0) {
                voucherDiscount = total;
            }
            total = total.subtract(voucherDiscount);
        }
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }
        this.totalAmount = total.setScale(2, RoundingMode.HALF_UP);
    }
    // Dùng trong DTO để format thời gian
    public String getFormattedCreatedAt() {
        return createdAt != null
                ? createdAt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                : "--/--/---- --:--:--";
    }
}