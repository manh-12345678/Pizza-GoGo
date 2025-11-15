// package Group5_pizza.Pizza_GoGo.DTO;
// ProductIngredientDTO.java
package Group5_pizza.Pizza_GoGo.DTO;
import lombok.Data;
import java.math.BigDecimal;
/**
 * DTO đại diện cho một dòng nguyên vật liệu trong form Product
 */
@Data
public class ProductIngredientDTO {
    private Integer ingredientId;
    private String ingredientName; // Dùng để hiển thị
    private BigDecimal quantityUsed; // Số lượng NVL
    private String unit; // Đơn vị (kg, g, pcs)
}