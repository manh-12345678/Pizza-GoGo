package Group5_pizza.Pizza_GoGo.DTO;

import lombok.Data;

@Data
public class ComboDetailDTO {
    private Integer comboDetailId;
    private Integer comboId;
    private Integer productId;
    private String productName;
    private Integer quantity;
}
