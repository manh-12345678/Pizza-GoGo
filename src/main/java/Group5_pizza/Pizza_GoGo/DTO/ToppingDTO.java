// package Group5_pizza.Pizza_GoGo.DTO;
// ToppingDTO.java
package Group5_pizza.Pizza_GoGo.DTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ToppingDTO {
    private Integer toppingId;
    private String name;
    private BigDecimal price;
    // ❗ Danh sách nguyên vật liệu cần thiết
    private List<ToppingIngredientDTO> ingredients = new ArrayList<>();
}