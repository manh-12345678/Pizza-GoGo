// package Group5_pizza.Pizza_GoGo.DTO;
// IngredientDTO.java
package Group5_pizza.Pizza_GoGo.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredientDTO {
    private Integer ingredientId;
    private String name;
    private String unit;
    private BigDecimal stockQuantity;
    private BigDecimal unitPrice;
    private BigDecimal minimumStock;
    private Boolean isLowStock; // Computed field
}