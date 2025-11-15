package Group5_pizza.Pizza_GoGo.model.cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuCartItem {
    private String id;
    private CartItemType type;
    private Integer productId;
    private Integer comboId;
    private String name;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal originalUnitPrice;
    private BigDecimal discountPerUnit;
    private BigDecimal totalPrice;
    @Builder.Default
    private List<MenuCartComboItem> comboItems = new ArrayList<>();
}

