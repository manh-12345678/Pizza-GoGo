// package Group5_pizza.Pizza_GoGo.model;
// Ingredient.java
package Group5_pizza.Pizza_GoGo.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonManagedReference; // ❗ THÊM IMPORT
import lombok.EqualsAndHashCode; // ❗ THÊM IMPORT
@Entity
@Table(name = "Ingredients")
@Data
@EqualsAndHashCode(exclude = {"productIngredients", "toppingIngredients"}) // ❗ FIX: Exclude để tránh cycle (nếu có bidirectional calls)
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IngredientId")
    private Integer ingredientId;
    @Column(name = "Name", nullable = false, length = 200)
    private String name;
    @Column(name = "Unit", nullable = false, length = 50)
    private String unit; // e.g., "kg", "g", "pcs", "ml", "l"
    @Column(name = "StockQuantity", nullable = false)
    private BigDecimal stockQuantity = BigDecimal.ZERO;
    @Column(name = "UnitPrice")
    private BigDecimal unitPrice = BigDecimal.ZERO; // Cost per unit (for cost calculation)
    @Column(name = "MinimumStock")
    private BigDecimal minimumStock;
    @Column(name = "CreatedAt", updatable = false, columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime createdAt;
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
    @Column(name = "IsDeleted", columnDefinition = "BIT DEFAULT 0")
    private Boolean isDeleted = false;
    // ❗ CẬP NHẬT: Thêm quan hệ tới bảng trung gian (LAZY để tránh cycle)
    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonManagedReference // ❗ FIX: Tránh cycle JSON
    private Set<ProductIngredient> productIngredients = new HashSet<>();
    // ❗ CẬP NHẬT: Thêm quan hệ tới bảng trung gian (LAZY để tránh cycle)
    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonManagedReference // ❗ FIX: Tránh cycle JSON (tương tự cho Topping)
    private Set<ToppingIngredient> toppingIngredients = new HashSet<>();
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}