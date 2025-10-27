package Group5_pizza.Pizza_GoGo.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderDTO {
    private Integer orderId;       // ID đơn hàng (JS đọc là 'id')
    private String table;          // Số bàn (JS đọc là 'table')
    private String status;         // Trạng thái (JS đọc là 'status')
    private String time;           // Thời gian đặt hàng (JS đọc là 'time')
    private BigDecimal totalAmount;  // Tổng tiền đơn hàng (JS đọc là 'total')
    private List<OrderItemDTO> items; // Danh sách các món hàng (JS đọc là 'items')
    // private Integer guests;     // Thêm nếu cần
}
