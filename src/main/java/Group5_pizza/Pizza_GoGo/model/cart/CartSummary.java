package Group5_pizza.Pizza_GoGo.model.cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartSummary {
    @Builder.Default
    private List<MenuCartItem> items = new ArrayList<>();
    private BigDecimal subtotal;
    private BigDecimal voucherDiscount;
    private BigDecimal total;
    private String voucherCode;
    private String message;
    private int totalQuantity;

    public static CartSummary from(MenuCart cart) {
        return CartSummary.builder()
                .items(new ArrayList<>(cart.getItems().values()))
                .subtotal(cart.getSubtotal())
                .voucherDiscount(cart.getVoucherDiscount())
                .total(cart.getTotal())
                .voucherCode(cart.getVoucherCode())
                .totalQuantity(cart.getItems().values().stream()
                        .mapToInt(MenuCartItem::getQuantity)
                        .sum())
                .build();
    }
}

