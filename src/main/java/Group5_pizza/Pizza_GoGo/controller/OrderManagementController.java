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

@Controller
@RequestMapping("/manager/orders")
@RequiredArgsConstructor
public class OrderManagementController {
    private final OrderService orderService;
    private final SimpMessagingTemplate messagingTemplate;
    private final DTOService dtoService;

    @GetMapping
    public String viewOrders(@RequestParam(required = false) String status, Model model) {
        List<Order> orders = orderService.getOrdersByStatus(status);
        model.addAttribute("orders", orders);
        model.addAttribute("status", status);
        return "orders/manage_orders";
    }

    @GetMapping("/list")
    @ResponseBody
    public List<OrderDTO> getOrdersAjax(@RequestParam(required = false) String status) {
        List<Order> orders = orderService.getOrdersByStatus(status);
        return orders.stream()
                .map(dtoService::convertToOrderDTO)
                .toList();
    }

    @PostMapping("/{orderId}/status")
    @ResponseBody
    public String updateOrderStatus(@PathVariable Integer orderId,
                                    @RequestParam String status) {
        boolean success = orderService.updateOrderStatus(orderId, status);
        if (success) {
            // Khi đổi trạng thái → gửi realtime tới tất cả client
            Order updatedOrder = orderService.getOrderById(orderId);
            OrderDTO dto = dtoService.convertToOrderDTO(updatedOrder);

            // Gửi tới tất cả client đang subscribe topic
            messagingTemplate.convertAndSend("/topic/orders/update", dto);
            return "success";
        }
        return "failed";
    }
}
