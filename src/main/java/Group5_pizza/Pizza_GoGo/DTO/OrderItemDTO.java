package Group5_pizza.Pizza_GoGo.DTO;

import lombok.Data;
import java.math.BigDecimal;

/**
 * DTO đại diện cho một món hàng, khớp với cấu trúc JS mong đợi (name, qty, price).
 */
@Data
public class OrderItemDTO {
    private String name;    // Tên sản phẩm
    private int qty;        // Số lượng
    private BigDecimal price; // Tổng tiền của món này (đơn giá * số lượng)
}