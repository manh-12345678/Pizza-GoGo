package Group5_pizza.Pizza_GoGo.controller;

import Group5_pizza.Pizza_GoGo.DTO.OrderDTO;
import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.service.DTOService;
import Group5_pizza.Pizza_GoGo.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors; // <-- Import

@Controller
@RequestMapping("/manager/orders")
@RequiredArgsConstructor
public class OrderManagementController {

    private final OrderService orderService;
    private final SimpMessagingTemplate messagingTemplate;
    private final DTOService dtoService;

    @GetMapping
    public String viewOrdersPage(Model model) {
        // Trả về tên view của file manage_orders.html
        return "orders/manage_orders"; // Sửa nếu cần
    }

    @GetMapping("/list")
    @ResponseBody
    public List<OrderDTO> getOrdersAjax(@RequestParam(required = false) String status) {
        // Gọi service mới để lấy đầy đủ chi tiết
        List<Order> orders = orderService.getOrdersByStatusWithDetails(status);
        return orders.stream()
                .map(dtoService::convertToOrderDTO)
                .collect(Collectors.toList());
    }

    @PostMapping("/{orderId}/status")
    @ResponseBody
    public String updateOrderStatus(@PathVariable Integer orderId,
                                    @RequestParam String status) {
        boolean success = orderService.updateOrderStatus(orderId, status.toUpperCase());
        if (success) {
            // Lấy lại Order VỚI ĐẦY ĐỦ CHI TIẾT
            Order updatedOrder = orderService.getOrderWithDetails(orderId);
            OrderDTO dto = dtoService.convertToOrderDTO(updatedOrder);
            messagingTemplate.convertAndSend("/topic/orders/update", dto);
            return "success";
        } else {
            return "failed";
        }
    }
}
