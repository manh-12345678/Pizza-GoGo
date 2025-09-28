package Group5_pizza.Pizza_GoGo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import Group5_pizza.Pizza_GoGo.DTO.OrderDTO;
import Group5_pizza.Pizza_GoGo.DTO.OrderDetailDTO;
import Group5_pizza.Pizza_GoGo.model.Order;

@Service
public class DTOService {

    public OrderDTO convertToOrderDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getOrderId());

        List<OrderDetailDTO> items = order.getOrderDetails().stream().map(od -> {
            OrderDetailDTO item = new OrderDetailDTO();
            item.setProductName(od.getProduct().getName());
            item.setQuantity(od.getQuantity());
            item.setUnitPrice(od.getProduct().getPrice());
            return item;
        }).collect(Collectors.toList());

        dto.setItems(items);

        java.math.BigDecimal total = items.stream()
                .map(i -> i.getUnitPrice().multiply(java.math.BigDecimal.valueOf(i.getQuantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        dto.setTotalAmount(total.longValue());

        return dto;
    }
}
