// package Group5_pizza.Pizza_GoGo.DTO;
// OrderDTO.java
package Group5_pizza.Pizza_GoGo.DTO;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
@Data
public class OrderDTO {
    private Integer orderId;
    private String table; // Chỉ số bàn (String)
    private String status;
    private String time; // HH:mm
    private BigDecimal totalAmount;
    private List<OrderItemDTO> items;
    @Data
    public static class OrderItemDTO {
        private Integer orderDetailId;
        private String name;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private List<ToppingDTO> toppings;
        @Data
        public static class ToppingDTO {
            private Integer toppingId;
            private String name;
            private BigDecimal price;
        }
    }
}