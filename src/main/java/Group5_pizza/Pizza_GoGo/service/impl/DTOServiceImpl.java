package Group5_pizza.Pizza_GoGo.service.impl;

import Group5_pizza.Pizza_GoGo.DTO.OrderDTO;
import Group5_pizza.Pizza_GoGo.DTO.OrderItemDTO; // Sử dụng DTO mới
import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.OrderDetail;
import Group5_pizza.Pizza_GoGo.service.DTOService; // Đảm bảo có Interface này
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DTOServiceImpl implements DTOService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public OrderDTO convertToOrderDTO(Order order) {
        if (order == null) return null;

        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getOrderId());

        // Lấy totalAmount đã được OrderServiceImpl tính toán chính xác
        dto.setTotalAmount(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);

        // Lấy các trường thông tin khác mà JS cần
        dto.setTable(order.getTable() != null ? String.valueOf(order.getTable().getTableNumber()) : "Mang về");
        dto.setStatus(order.getStatus() != null ? order.getStatus().toLowerCase() : "unknown"); // JS dùng chữ thường
        dto.setTime(order.getCreatedAt() != null ? order.getCreatedAt().format(TIME_FORMATTER) : "--:--");

        // Chuyển đổi OrderDetails (đã được lọc bởi ServiceImpl) sang List<OrderItemDTO>
        if (order.getOrderDetails() != null) {
            List<OrderItemDTO> itemDTOs = order.getOrderDetails().stream()
                    .map(this::convertToOrderItemDTO) // Gọi hàm helper đã sửa
                    .collect(Collectors.toList());
            dto.setItems(itemDTOs);
        } else {
            dto.setItems(Collections.emptyList()); // Đảm bảo items không rỗng
        }
        return dto;
    }

    /**
     * Chuyển đổi OrderDetail sang OrderItemDTO (khớp với JS: name, qty, price)
     * Tính toán tổng tiền cho từng dòng (price = unitPrice * quantity)
     */
    private OrderItemDTO convertToOrderItemDTO(OrderDetail detail) {
        OrderItemDTO itemDTO = new OrderItemDTO();

        itemDTO.setName(detail.getProduct() != null ? detail.getProduct().getName() : "Sản phẩm lỗi");

        int quantity = detail.getQuantity() != null ? detail.getQuantity() : 0;
        itemDTO.setQty(quantity);

        BigDecimal unitPrice = detail.getUnitPrice() != null ? detail.getUnitPrice() : BigDecimal.ZERO;
        itemDTO.setPrice(unitPrice.multiply(BigDecimal.valueOf(quantity))); // Tính tổng tiền dòng

        // Bỏ qua 'note' vì OrderDetail gốc không có

        return itemDTO;
    }
}