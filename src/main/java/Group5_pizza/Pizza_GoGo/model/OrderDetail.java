// package Group5_pizza.Pizza_GoGo.model;
// OrderDetail.java
package Group5_pizza.Pizza_GoGo.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
// --- SỬA LỖI: Đổi import từ List/ArrayList sang Set/HashSet ---
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "order_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_id")
    private Integer orderDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference // Ngăn vòng lặp ngược về Order
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "unit_price", columnDefinition = "DECIMAL(18,2)")
    private BigDecimal unitPrice;

    @Column(name = "discount", columnDefinition = "DECIMAL(18,2) DEFAULT 0.00")
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "note")
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_deleted", columnDefinition = "BIT DEFAULT 0")
    @Builder.Default
    private Boolean isDeleted = false;

    // --- SỬA LỖI "MultipleBagFetchException": Đổi List thành Set ---
    @OneToMany(mappedBy = "orderDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private Set<OrderDetailTopping> orderDetailToppings = new HashSet<>();

    // Tính thành tiền (product + topping - discount)
    public BigDecimal getSubtotal() {
        BigDecimal base = unitPrice.multiply(BigDecimal.valueOf(quantity)).subtract(discount);
        BigDecimal toppingTotal = orderDetailToppings.stream()
                .filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()))
                .map(OrderDetailTopping::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return base.add(toppingTotal);
    }
    public void addTopping(OrderDetailTopping topping) {
        if (orderDetailToppings == null) orderDetailToppings = new HashSet<>();
        orderDetailToppings.add(topping);
        topping.setOrderDetail(this);
    }
}