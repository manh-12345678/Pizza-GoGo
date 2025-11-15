// package Group5_pizza.Pizza_GoGo.model;
// ToppingIngredient.java
package Group5_pizza.Pizza_GoGo.model;
import com.fasterxml.jackson.annotation.JsonBackReference; // ❗ THÊM IMPORT
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import lombok.EqualsAndHashCode; // ❗ THÊM IMPORT cho Exclude
/**
 * Entity cho bảng trung gian Topping_Ingredients
 */
@Entity
@Table(name = "Topping_Ingredients")
@Data // Giữ @Data
@EqualsAndHashCode(exclude = {"topping", "ingredient"}) // ❗ FIX: Exclude parents để tránh cycle hashCode
@NoArgsConstructor
@AllArgsConstructor
public class ToppingIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ToppingIngredientId")
    private Integer toppingIngredientId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ToppingId")
    @ToString.Exclude
    @JsonBackReference // ❗ FIX: Tránh cycle JSON
    private Topping topping;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IngredientId")
    @ToString.Exclude
    @JsonBackReference // ❗ FIX: Tránh cycle JSON
    private Ingredient ingredient;
    // Số lượng nguyên vật liệu (ví dụ: 0.05 kg phô mai) cần cho 1 topping
    @Column(name = "QuantityUsed", nullable = false)
    private BigDecimal quantityUsed;
}