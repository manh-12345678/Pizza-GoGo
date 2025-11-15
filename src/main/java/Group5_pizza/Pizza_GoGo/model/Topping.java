// package Group5_pizza.Pizza_GoGo.model;
// Topping.java
package Group5_pizza.Pizza_GoGo.model;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.EqualsAndHashCode; // ❗ THÊM IMPORT cho Exclude
@Entity
@Table(name = "Toppings")
@Data // Giữ @Data
@EqualsAndHashCode(exclude = {"productToppings", "toppingIngredients"}) // ❗ FIX: Exclude collections để tránh cycle hashCode
@NoArgsConstructor
@AllArgsConstructor
public class Topping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ToppingId")
    private Integer toppingId;
    @Column(name = "Name", nullable = false, length = 100)
    private String name;
    @Column(name = "Price", nullable = false) // Bỏ updatable, insertable
    private BigDecimal price;
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
    @Column(name = "IsDeleted", nullable = false)
    private Boolean isDeleted = false;
    @OneToMany(mappedBy = "topping")
    @ToString.Exclude // ❗ SỬA LỖI: Thêm dòng này
    @JsonManagedReference
    private List<ProductTopping> productToppings;
    // ❗ FIX: Đổi EAGER → LAZY để tránh StackOverflow
    @OneToMany(mappedBy = "topping", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude // ❗ SỬA LỖI: Thêm dòng này
    @JsonManagedReference
    private Set<ToppingIngredient> toppingIngredients = new HashSet<>();
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}