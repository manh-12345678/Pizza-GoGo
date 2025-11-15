// package Group5_pizza.Pizza_GoGo.model;
// ProductIngredient.java
package Group5_pizza.Pizza_GoGo.model;
import com.fasterxml.jackson.annotation.JsonBackReference; // ❗ THÊM IMPORT
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import lombok.EqualsAndHashCode; // ❗ THÊM IMPORT cho Exclude
/**
 * Entity cho bảng trung gian Product_Ingredients
 */
@Entity
@Table(name = "Product_Ingredients")
@Data // Giữ @Data
@EqualsAndHashCode(exclude = {"product", "ingredient"}) // ❗ FIX: Exclude parents để tránh cycle hashCode
@NoArgsConstructor
@AllArgsConstructor
public class ProductIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductIngredientId")
    private Integer productIngredientId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductId")
    @ToString.Exclude
    @JsonBackReference // ❗ FIX: Tránh cycle JSON
    private Product product;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IngredientId")
    @ToString.Exclude
    @JsonBackReference // ❗ FIX: Tránh cycle JSON
    private Ingredient ingredient;
    // Số lượng nguyên vật liệu (ví dụ: 0.1 kg bột) cần cho 1 sản phẩm
    @Column(name = "QuantityUsed", nullable = false)
    private BigDecimal quantityUsed;
}