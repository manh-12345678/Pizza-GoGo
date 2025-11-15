// package Group5_pizza.Pizza_GoGo.DTO;
// ToppingIngredientDTO.java
package Group5_pizza.Pizza_GoGo.DTO;
import lombok.Data;
import java.math.BigDecimal;
/**
 * DTO đại diện cho một dòng nguyên vật liệu trong form Topping
 */
@Data
public class ToppingIngredientDTO {
    private Integer ingredientId;
    private String ingredientName;
    private BigDecimal quantityUsed;
    private String unit;
}