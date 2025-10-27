package Group5_pizza.Pizza_GoGo.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import Group5_pizza.Pizza_GoGo.DTO.OrderDTO;
import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.Product;
import Group5_pizza.Pizza_GoGo.model.RestaurantTable;
import Group5_pizza.Pizza_GoGo.service.DTOService;
import Group5_pizza.Pizza_GoGo.service.OrderDetailService;
import Group5_pizza.Pizza_GoGo.service.OrderService;
import Group5_pizza.Pizza_GoGo.service.ProductService;
import Group5_pizza.Pizza_GoGo.service.RestaurantTableService;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final RestaurantTableService tableService;
    private final OrderService orderService;
    private final OrderDetailService orderDetailService;
    private final ProductService productService;
    private final SimpMessagingTemplate messagingTemplate;
    private final DTOService dtoService;

    public OrderController(RestaurantTableService tableService,
                           OrderService orderService,
                           OrderDetailService orderDetailService,
                           ProductService productService,
                           SimpMessagingTemplate messagingTemplate,
                           DTOService dtoService) {
        this.tableService = tableService;
        this.orderService = orderService;
        this.orderDetailService = orderDetailService;
        this.productService = productService;
        this.messagingTemplate = messagingTemplate;
        this.dtoService = dtoService;
    }


    @GetMapping("/table/{tableId}")
    public String chooseMenu(@PathVariable Integer tableId, HttpSession session, Model model) {
        RestaurantTable table = tableService.getTableById(tableId);

        Order order = orderService.getLatestOrderByTable(table);

        if (order == null || (order.getStatus() != null && order.getStatus().equalsIgnoreCase("COMPLETED"))) {
            order = orderService.createNewOrderForTable(table);
        } else {
            order = orderService.getOrderWithDetails(order.getOrderId());
        }

        session.setAttribute("currentOrder_" + tableId, order);

        // ĐÃ SỬA: Gọi phương thức mới để lấy sản phẩm KÈM topping
        List<Product> products = productService.getAllAvailableProducts();

        // Gom theo category
        List<String> categories = products.stream()
                .map(p -> p.getCategory() != null ? p.getCategory().getCategoryName() : "Uncategorized")
                .distinct()
                .toList();

        Map<String, List<Product>> productsByCategory = categories.stream()
                .collect(Collectors.toMap(
                        c -> c,
                        c -> products.stream()
                                .filter(p -> (p.getCategory() != null && c.equals(p.getCategory().getCategoryName()))
                                        || (p.getCategory() == null && c.equals("Uncategorized")))
                                .toList()
                ));

        model.addAttribute("table", table);
        model.addAttribute("order", order);
        model.addAttribute("categories", categories);
        model.addAttribute("productsByCategory", productsByCategory);

        return "orders/choose_menu";
    }

    @PostMapping("/add")
    @ResponseBody
    public String addProductToOrderAjax(@RequestParam Integer orderId,
                                        @RequestParam Integer productId,
                                        @RequestParam(defaultValue = "1") Integer quantity,
                                        @RequestParam(required = false) String note,
                                        HttpSession session) {
        try {
            // Lấy order và product
            // (Bạn phải đảm bảo getOrderWithDetails đã được sửa lỗi logic từ trước)
            Order order = orderService.getOrderWithDetails(orderId);
            Product product = productService.getProductById(productId);

            // Lưu ý: addOrUpdateOrderDetail phải được sửa để LƯU 'note'
            orderDetailService.addOrUpdateOrderDetail(order, product, quantity, note);

            // Cập nhật lại order sau khi thêm
            Order updatedOrder = orderService.getOrderWithDetails(orderId);

            session.setAttribute("currentOrder_" + updatedOrder.getTable().getTableId(), updatedOrder);

            // Gửi DTO (Bạn cần đảm bảo DTOService xử lý đúng)
            OrderDTO dto = dtoService.convertToOrderDTO(updatedOrder);
            messagingTemplate.convertAndSend("/topic/orders/" + updatedOrder.getTable().getTableId(), dto);

            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }

    @GetMapping("/cart/{orderId}")
    public String viewCart(@PathVariable Integer orderId, Model model) {
        // (Bạn phải đảm bảo getOrderWithDetails đã được sửa lỗi logic từ trước)
        Order order = orderService.getOrderWithDetails(orderId);
        model.addAttribute("order", order);
        return "orders/cart";
    }
}
