package Group5_pizza.Pizza_GoGo.DTO;

import java.math.BigDecimal;
import java.util.List;

import Group5_pizza.Pizza_GoGo.model.Combo;
import Group5_pizza.Pizza_GoGo.model.ComboDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuComboView {
    private Combo combo;
    private List<ComboDetail> details;
    private BigDecimal basePrice;
    private BigDecimal finalPrice;
    private BigDecimal discountAmount;
    private BigDecimal discountPercent;
    private String description;
}

