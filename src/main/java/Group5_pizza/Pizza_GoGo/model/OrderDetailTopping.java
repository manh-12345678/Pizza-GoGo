// package Group5_pizza.Pizza_GoGo.model;
// OrderDetailTopping.java
package Group5_pizza.Pizza_GoGo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_detail_toppings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = { "orderDetail" }) // Exclude orderDetail để tránh cycle và lazy loading khi add vào Set
public class OrderDetailTopping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_topping_id")
    private Integer orderDetailToppingId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_detail_id")
    @JsonBackReference
    private OrderDetail orderDetail;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topping_id")
    private Topping topping;
    @Column(name = "price", columnDefinition = "DECIMAL(18,2)")
    private BigDecimal price;
    @Column(name = "is_deleted", columnDefinition = "BIT DEFAULT 0")
    @Builder.Default
    private Boolean isDeleted = false;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}