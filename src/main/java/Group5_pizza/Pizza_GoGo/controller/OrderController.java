package Group5_pizza.Pizza_GoGo.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import Group5_pizza.Pizza_GoGo.DTO.OrderResponseDTO;
import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.OrderDetail;
import Group5_pizza.Pizza_GoGo.model.Product;
import Group5_pizza.Pizza_GoGo.model.RestaurantTable;
import Group5_pizza.Pizza_GoGo.model.Topping; // Giả sử bạn có model Topping
import Group5_pizza.Pizza_GoGo.service.DTOService;
import Group5_pizza.Pizza_GoGo.service.OrderDetailService;
import Group5_pizza.Pizza_GoGo.service.OrderService;
import Group5_pizza.Pizza_GoGo.service.ProductService;
import Group5_pizza.Pizza_GoGo.service.RestaurantTableService;
import Group5_pizza.Pizza_GoGo.service.ToppingService; // Giả sử bạn có ToppingService để lấy topping theo name
import Group5_pizza.Pizza_GoGo.util.ThymeleafUtils;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final RestaurantTableService tableService;
    private final OrderService orderService;
    private final OrderDetailService orderDetailService;
    private final ProductService productService;
    private final ToppingService toppingService; // Thêm ToppingService
    private final SimpMessagingTemplate messagingTemplate;
    private final DTOService dtoService;
    private final ThymeleafUtils thymeleafUtils;

    public OrderController(RestaurantTableService tableService,
            OrderService orderService,
            OrderDetailService orderDetailService,
            ProductService productService,
            ToppingService toppingService, // Inject ToppingService
            SimpMessagingTemplate messagingTemplate,
            DTOService dtoService,
            ThymeleafUtils thymeleafUtils) {
        this.tableService = tableService;
        this.orderService = orderService;
        this.orderDetailService = orderDetailService;
        this.productService = productService;
        this.toppingService = toppingService;
        this.messagingTemplate = messagingTemplate;
        this.dtoService = dtoService;
        this.thymeleafUtils = thymeleafUtils;
    }

    @GetMapping("/search")
    @ResponseBody
    public List<OrderResponseDTO> searchOrders(
            @RequestParam(required = false) Integer orderId,
            @RequestParam(required = false) Integer tableNumber) {
        List<Order> orders = orderService.searchOrders(orderId, tableNumber);
        return orders.stream()
                .map(dtoService::convertToOrderDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/table/{tableId}")
    public String chooseMenu(@PathVariable Integer tableId, HttpSession session, Model model) {
        RestaurantTable table = tableService.getTableById(tableId);

        // Kiểm tra order mới nhất của bàn
        Order existingOrder = orderService.getLatestOrderByTable(table);
        boolean hasNonCompletedOrder = existingOrder != null &&
                existingOrder.getStatus() != null &&
                !existingOrder.getStatus().equalsIgnoreCase("COMPLETED");
        boolean isOrderCompleted = existingOrder != null &&
                existingOrder.getStatus() != null &&
                existingOrder.getStatus().equalsIgnoreCase("COMPLETED");

        if (!"UNAVAILABLE".equalsIgnoreCase(table.getStatus())) {
            tableService.updateTableStatus(tableId, "UNAVAILABLE");
            // Reload table để có status mới
            table = tableService.getTableById(tableId);
        }

        Order order = existingOrder;

        if (order == null || isOrderCompleted) {
            // Chỉ tạo order mới khi không có order hoặc order cuối cùng đã COMPLETED
            order = orderService.createNewOrderForTable(table);
        } else if (hasNonCompletedOrder) {
            // Nếu có order chưa COMPLETED, dùng order đó
            order = orderService.getOrderWithDetails(order.getOrderId());
        }

        // Đảm bảo orderDetails luôn được khởi tạo (không null)
        if (order.getOrderDetails() == null) {
            order.setOrderDetails(new java.util.ArrayList<>());
        }

        session.setAttribute("currentOrder_" + tableId, order);
        List<Product> products = productService.getAllProducts();

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
                                .toList()));

        model.addAttribute("table", table);
        model.addAttribute("order", order);
        model.addAttribute("categories", categories);
        model.addAttribute("productsByCategory", productsByCategory);

        return "orders/choose_menu";
    }

    // Lấy toppings cho một product - ĐẶT TRƯỚC để tránh conflict
    @GetMapping("/products/{productId}/toppings")
    @ResponseBody
    public List<Group5_pizza.Pizza_GoGo.DTO.ProductDTO.ToppingDTO> getToppingsForProduct(
            @PathVariable Integer productId) {
        try {
            // Load product với toppings để tránh LazyInitializationException
            Product product = productService.getProductByIdWithToppings(productId);
            if (product == null) {
                return List.of();
            }
            List<Topping> toppings = product.getToppings();
            if (toppings == null || toppings.isEmpty()) {
                return List.of();
            }
            return toppings.stream()
                    .filter(t -> t != null && !Boolean.TRUE.equals(t.getIsDeleted()))
                    .map(t -> new Group5_pizza.Pizza_GoGo.DTO.ProductDTO.ToppingDTO(t.getToppingId(), t.getName(),
                            t.getPrice()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // Thêm topping vào orderDetail - ĐẶT TRƯỚC endpoint /add để tránh conflict
    @PostMapping(value = "/orderdetails/{orderDetailId}/add-topping", produces = "text/plain")
    @ResponseBody
    public String addToppingToOrderDetail(
            @PathVariable Integer orderDetailId,
            @RequestParam Integer toppingId,
            HttpSession session) {
        try {
            System.out.println("=== ADD TOPPING CALLED ===");
            System.out.println("orderDetailId: " + orderDetailId);
            System.out.println("toppingId: " + toppingId);

            OrderDetail orderDetail = orderDetailService.getOrderDetailById(orderDetailId);
            if (orderDetail == null) {
                System.out.println("ERROR: OrderDetail không tồn tại");
                return "error: OrderDetail không tồn tại";
            }

            System.out.println("OrderDetail found: " + orderDetail.getOrderDetailId());
            System.out.println(
                    "Product: " + (orderDetail.getProduct() != null ? orderDetail.getProduct().getName() : "null"));

            // Lấy orderId trực tiếp để tránh vòng lặp
            Integer orderId = orderDetail.getOrder().getOrderId();
            System.out.println("Order ID: " + orderId);

            // Gọi service để add topping
            OrderDetail updatedDetail = orderService.addToppingToOrderDetail(orderDetailId, toppingId);
            System.out.println("Topping added successfully");
            System.out.println("Updated OrderDetail ID: " + updatedDetail.getOrderDetailId());
            System.out.println("Number of toppings after add: " +
                    (updatedDetail.getOrderDetailToppings() != null ? updatedDetail.getOrderDetailToppings().size()
                            : 0));

            // Load lại order để cập nhật
            Order updatedOrder = orderService.getOrderWithDetails(orderId);
            System.out.println("=== ORDER DETAILS AFTER UPDATE ===");
            System.out.println("Order ID: " + updatedOrder.getOrderId());
            System.out.println("Total Amount: " + updatedOrder.getTotalAmount());
            System.out.println("Number of OrderDetails: " +
                    (updatedOrder.getOrderDetails() != null ? updatedOrder.getOrderDetails().size() : 0));

            // Debug từng OrderDetail và toppings
            if (updatedOrder.getOrderDetails() != null) {
                for (OrderDetail detail : updatedOrder.getOrderDetails()) {
                    if (detail != null && (detail.getIsDeleted() == null || !detail.getIsDeleted())) {
                        System.out.println("  - OrderDetail ID: " + detail.getOrderDetailId() +
                                ", Product: " + (detail.getProduct() != null ? detail.getProduct().getName() : "null") +
                                ", Quantity: " + detail.getQuantity() +
                                ", Unit Price: " + detail.getUnitPrice() +
                                ", Subtotal: " + detail.getSubtotal());

                        if (detail.getOrderDetailToppings() != null && !detail.getOrderDetailToppings().isEmpty()) {
                            System.out.println("    Toppings (" + detail.getOrderDetailToppings().size() + "):");
                            for (Group5_pizza.Pizza_GoGo.model.OrderDetailTopping odt : detail
                                    .getOrderDetailToppings()) {
                                if (odt != null && (odt.getIsDeleted() == null || !odt.getIsDeleted())
                                        && odt.getTopping() != null) {
                                    System.out.println("      - " + odt.getTopping().getName() +
                                            " (Price: " + odt.getPrice() +
                                            ", ID: " + odt.getOrderDetailToppingId() + ")");
                                }
                            }
                        } else {
                            System.out.println("    No toppings");
                        }
                    }
                }
            }

            if (updatedOrder.getTable() != null) {
                System.out.println("Table ID: " + updatedOrder.getTable().getTableId());
                session.setAttribute("currentOrder_" + updatedOrder.getTable().getTableId(), updatedOrder);

                // Chỉ render fragment và gửi WebSocket nếu có table (cho cart page)
                // Nếu không có table (order_detail page), chỉ return success để reload
                try {
                    String html = thymeleafUtils.renderFragment(
                            "orders/cart :: orderListFragment",
                            Map.of("order", updatedOrder));

                    System.out.println("Fragment rendered successfully, length: " + (html != null ? html.length() : 0));
                    if (html != null) {
                        messagingTemplate.convertAndSend("/topic/orders/" + updatedOrder.getTable().getTableId(), html);
                        System.out.println(
                                "WebSocket message sent to /topic/orders/" + updatedOrder.getTable().getTableId());
                    }
                } catch (Exception e) {
                    // Nếu không render được fragment (ví dụ: gọi từ order_detail page),
                    // không sao, chỉ cần return success để page reload
                    System.out.println(
                            "Could not render fragment (might be called from order_detail page): " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("No table associated with order (likely order_detail page)");
            }

            System.out.println("=== ADD TOPPING SUCCESS ===");
            System.out.println("Returning: success");
            return "success";
        } catch (Exception e) {
            System.out.println("=== ADD TOPPING ERROR ===");
            System.out.println("Error message: " + e.getMessage());
            System.out.println("Error class: " + e.getClass().getName());
            e.printStackTrace();
            String errorResponse = "error: " + e.getMessage();
            System.out.println("Returning: " + errorResponse);
            return errorResponse;
        }
    }

    // Trong OrderController.java - ĐẶT SAU endpoint add-topping
    // Đổi thành /add-product để tránh conflict với /orderdetails/.../add-topping
    @PostMapping("/add-product")
    @ResponseBody
    public String addProductToOrderAjax(
            @RequestParam Integer orderId,
            @RequestParam Integer productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            @RequestParam(required = false) String note,
            @RequestParam(required = false) String toppingName,
            HttpSession session) {
        try {
            System.out.println("=== ADD PRODUCT CALLED (NOT TOPPING) ===");
            System.out.println("orderId: " + orderId);
            System.out.println("productId: " + productId);
            Order order = orderService.getOrderWithDetails(orderId);
            Product product = productService.getProductById(productId);

            Topping topping = null;
            if (toppingName != null && !toppingName.trim().isEmpty()) {
                topping = toppingService.getToppingByName(toppingName);
            }

            orderDetailService.addOrUpdateOrderDetail(order, product, quantity, note, topping);

            Order updatedOrder = orderService.getOrderWithDetails(orderId);
            if (updatedOrder.getTable() != null) {
                session.setAttribute("currentOrder_" + updatedOrder.getTable().getTableId(), updatedOrder);
            }

            String html = thymeleafUtils.renderFragment(
                    "orders/cart :: orderListFragment",
                    Map.of("order", updatedOrder));

            if (updatedOrder.getTable() != null && html != null) {
                messagingTemplate.convertAndSend("/topic/orders/" + updatedOrder.getTable().getTableId(), html);
            }

            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }

    @GetMapping("/cart/{orderId}")
    public String viewCart(@PathVariable Integer orderId, Model model) {
        Order order = orderService.getOrderWithDetails(orderId);
        model.addAttribute("table", order.getTable());
        model.addAttribute("order", order);
        return "orders/cart";
    }

    // Xóa topping khỏi orderDetail
    @PostMapping("/{orderId}/delete-topping")
    @ResponseBody
    public String deleteTopping(
            @PathVariable Integer orderId,
            @RequestParam Integer toppingId,
            HttpSession session) {
        try {
            orderService.deleteToppingFromOrder(toppingId);

            Order updatedOrder = orderService.getOrderWithDetails(orderId);
            if (updatedOrder.getTable() != null) {
                session.setAttribute("currentOrder_" + updatedOrder.getTable().getTableId(), updatedOrder);
            }

            String html = thymeleafUtils.renderFragment(
                    "orders/cart :: orderListFragment",
                    Map.of("order", updatedOrder));

            if (updatedOrder.getTable() != null && html != null) {
                messagingTemplate.convertAndSend("/topic/orders/" + updatedOrder.getTable().getTableId(), html);
            }

            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }
}