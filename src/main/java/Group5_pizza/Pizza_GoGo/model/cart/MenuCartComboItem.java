package Group5_pizza.Pizza_GoGo.model.cart;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuCartComboItem {
    private Integer productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
}

