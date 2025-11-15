// package Group5_pizza.Pizza_GoGo.controller;
// OrderManagementController.java
package Group5_pizza.Pizza_GoGo.controller;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import Group5_pizza.Pizza_GoGo.DTO.OrderResponseDTO;
import Group5_pizza.Pizza_GoGo.DTO.ProductDTO;
import Group5_pizza.Pizza_GoGo.DTO.TableDTO; // Giả sử có DTO cho bàn
import Group5_pizza.Pizza_GoGo.model.Account;
import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.OrderDetail;
import Group5_pizza.Pizza_GoGo.model.Product;
import Group5_pizza.Pizza_GoGo.model.RestaurantTable;
import Group5_pizza.Pizza_GoGo.model.Topping;
import jakarta.servlet.http.HttpSession;
import Group5_pizza.Pizza_GoGo.service.DTOService;
import Group5_pizza.Pizza_GoGo.service.OrderDetailService;
import Group5_pizza.Pizza_GoGo.service.OrderService;
import Group5_pizza.Pizza_GoGo.service.PaymentEntityService;
import Group5_pizza.Pizza_GoGo.service.ProductService;
import Group5_pizza.Pizza_GoGo.service.RestaurantTableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Controller
@RequestMapping("/manager/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderManagementController {
    private final OrderService orderService;
    private final DTOService dtoService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ProductService productService;
    private final OrderDetailService orderDetailService;
    private final RestaurantTableService tableService;
    private final PaymentEntityService paymentEntityService;
    // 1. Trang quản lý đơn hàng
    @GetMapping
    public String viewOrdersPage(Model model) {
        return "orders/manage_orders";
    }
    // 2. Lấy danh sách đơn hàng (AJAX)
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<?> getOrdersAjax(@RequestParam(required = false) String status) {
        try {
            log.info("Lấy danh sách đơn hàng - trạng thái: {}", status);
            List<Order> orders = orderService.getOrdersByStatusWithDetails(status);
            List<OrderResponseDTO> dtos = orders.stream()
                    .map(order -> {
                        try {
                            return dtoService.convertToOrderDTO(order);
                        } catch (Exception e) {
                            log.error("Lỗi khi convert Order {} sang DTO: {}", order.getOrderId(), e.getMessage(), e);
                            return null;
                        }
                    })
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách đơn hàng: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi tải danh sách đơn hàng: " + e.getMessage()));
        }
    }
    // 3. Tìm kiếm đơn hàng
    @GetMapping("/search")
    @ResponseBody
    public List<OrderResponseDTO> searchOrders(
            @RequestParam(required = false) Integer orderId,
            @RequestParam(required = false) Integer tableNumber) {
        log.info("Tìm kiếm đơn: orderId={}, tableNumber={}", orderId, tableNumber);
        List<Order> orders = orderService.searchOrders(orderId, tableNumber);
        return orders.stream()
                .map(dtoService::convertToOrderDTO)
                .collect(Collectors.toList());
    }
    // 4. Tạo đơn mới cho bàn
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<?> createOrder(@RequestParam Integer tableId) {
        log.info("Tạo đơn mới cho bàn: {}", tableId);
        try {
            // Kiểm tra trạng thái bàn
            RestaurantTable table = tableService.getTableById(tableId);
            if ("UNAVAILABLE".equalsIgnoreCase(table.getStatus())) {
                log.warn("Không thể tạo đơn cho bàn {} - bàn đang UNAVAILABLE", tableId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Bàn này hiện không khả dụng. Không thể tạo đơn hàng mới."));
            }
            
            Order order = orderService.createOrderForTable(tableId);
            order.setStatus("PENDING");
            order = orderService.saveOrder(order);
            
            // Chuyển bàn sang UNAVAILABLE khi tạo đơn
            tableService.updateTableStatus(tableId, "UNAVAILABLE");
            
            Order fullOrder = orderService.getOrderWithDetails(order.getOrderId());
            OrderResponseDTO dto = dtoService.convertToOrderDTO(fullOrder);
            broadcastUpdate(dto);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("Lỗi khi tạo đơn cho bàn {}: {}", tableId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi tạo đơn: " + e.getMessage()));
        }
    }

    // 5. Thêm món vào đơn
    @PostMapping("/{orderId}/add-product")
    @ResponseBody
    public ResponseEntity<?> addProduct(
            @PathVariable Integer orderId,
            @RequestParam Integer productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            @RequestParam(required = false) String note) {
        log.info("Thêm món vào đơn {}: productId={}, qty={}, note={}", orderId, productId, quantity, note);
        try {
            Order order = orderService.getOrderWithDetails(orderId);
            Product product = productService.getProductById(productId);
            orderDetailService.addOrUpdateOrderDetail(order, product, quantity, note);
            Order updatedOrder = orderService.getOrderWithDetails(orderId);
            OrderResponseDTO dto = dtoService.convertToOrderDTO(updatedOrder);
            broadcastUpdate(dto);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            log.warn("Lỗi khi thêm món: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            log.error("Lỗi khi thêm món: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // 6. Thêm topping
    @PostMapping("/{orderDetailId}/add-topping")
    @ResponseBody
    public ResponseEntity<OrderResponseDTO> addTopping(
            @PathVariable Integer orderDetailId,
            @RequestParam Integer toppingId) {
        log.info("Thêm topping {} vào orderDetail {}", toppingId, orderDetailId);
        try {
            OrderDetail od = orderService.addToppingToOrderDetail(orderDetailId, toppingId);
            Order updatedOrder = orderService.getOrderWithDetails(od.getOrder().getOrderId());
            OrderResponseDTO dto = dtoService.convertToOrderDTO(updatedOrder);
            broadcastUpdate(dto);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            log.warn("Lỗi khi thêm topping: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    // 7. Cập nhật trạng thái
    @PostMapping("/{orderId}/status")
    @ResponseBody
    public ResponseEntity<String> updateOrderStatus(
            @PathVariable Integer orderId,
            @RequestParam String status) {
        log.info("Cập nhật trạng thái đơn {} → {}", orderId, status);
        try {
            boolean success = orderService.updateOrderStatus(orderId, status.toUpperCase());
            if (success) {
                Order updatedOrder = orderService.getOrderWithDetails(orderId);
                OrderResponseDTO dto = dtoService.convertToOrderDTO(updatedOrder);
                broadcastUpdate(dto);
                return ResponseEntity.ok("success");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("failed: Không thể cập nhật trạng thái");
            }
        } catch (Exception e) {
            log.error("Lỗi cập nhật trạng thái", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("failed: " + e.getMessage());
        }
    }


    // 8. Hủy đơn (chỉ trong 5 phút)
    @PostMapping("/{orderId}/cancel")
    @ResponseBody
    public ResponseEntity<String> cancelOrder(@PathVariable Integer orderId) {
        log.info("Yêu cầu hủy đơn {}", orderId);
        Order order = orderService.getOrderWithDetails(orderId);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("failed: Đơn không tồn tại");
        }
        LocalDateTime now = LocalDateTime.now();
        long minutesFromCreation = ChronoUnit.MINUTES.between(order.getCreatedAt(), now);
        if (minutesFromCreation > 5) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("failed: Không thể hủy (đã quá 5 phút)");
        }
        boolean hasLongCookingItem = order.getOrderDetails().stream()
                .anyMatch(od -> {
                    LocalDateTime addedAt = od.getCreatedAt();
                    return addedAt != null && ChronoUnit.MINUTES.between(addedAt, now) > 10;
                });
        if (hasLongCookingItem) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("failed: Có món đã gọi lâu, không thể hủy");
        }
        boolean success = orderService.updateOrderStatus(orderId, "CANCELLED");
        if (success) {
            OrderResponseDTO dto = dtoService.convertToOrderDTO(orderService.getOrderWithDetails(orderId));
            broadcastUpdate(dto);
            return ResponseEntity.ok("success");
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("failed");
    }
    // Thêm API xóa product (OrderDetail)
    @PostMapping("/{orderId}/delete-product")
    @ResponseBody
    public ResponseEntity<OrderResponseDTO> deleteProduct(
            @PathVariable Long orderId,
            @RequestParam Integer orderDetailId) {
        try {
            orderDetailService.deleteOrderDetail(orderId,orderDetailId);
            Order updatedOrder = orderService.getOrderWithDetails(Math.toIntExact(orderId));
            OrderResponseDTO dto = dtoService.convertToOrderDTO(updatedOrder);
            broadcastUpdate(dto);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("Lỗi xóa món", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
    // Thêm API xóa topping (OrderDetailTopping)
    @PostMapping("/{orderId}/delete-topping")
    @ResponseBody
    public ResponseEntity<OrderResponseDTO> deleteTopping(
            @PathVariable Integer orderId,
            @RequestParam Integer toppingId) {
        try {
            orderService.deleteToppingFromOrder(toppingId); // Giả sử method mới ở service
            Order updatedOrder = orderService.getOrderWithDetails(orderId);
            OrderResponseDTO dto = dtoService.convertToOrderDTO(updatedOrder);
            broadcastUpdate(dto);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("Lỗi xóa topping", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
    // Thêm API lấy list bàn (cho modal create)
    @GetMapping("/tables")
    @ResponseBody
    public List<TableDTO> getTables() {
        return tableService.getAllTables().stream()
                .map(t -> new TableDTO(t.getTableId(), t.getTableName()))
                .collect(Collectors.toList());
    }
    // Thêm API để lấy list sản phẩm (cho modal)
    @GetMapping("/products")
    @ResponseBody
    public List<ProductDTO> getProducts() {
        List<Product> products = productService.getAllProducts();
        return products.stream()
                .map(p -> {
                    ProductDTO dto = new ProductDTO();
                    dto.setProductId(p.getProductId());
                    dto.setName(p.getName());
                    dto.setPrice(p.getPrice());
                    return dto;
                })
                .collect(Collectors.toList());
    }
    // Helper: Gửi cập nhật qua WebSocket
    private void broadcastUpdate(OrderResponseDTO dto) {
        if (dto == null) {
            return;
        }
        try {
            messagingTemplate.convertAndSend("/topic/orders/update", dto);
            log.debug("Đã gửi WebSocket update cho đơn {}", dto.getOrderId());
        } catch (Exception e) {
            log.error("Lỗi gửi WebSocket update", e);
        }
    }


    // Thêm method
    @GetMapping("/orderdetails/{orderDetailId}/toppings")
    @ResponseBody
    public List<ProductDTO.ToppingDTO> getToppingsForOrderDetail(@PathVariable Integer orderDetailId) {
        try {
            OrderDetail detail = orderDetailService.getOrderDetailById(orderDetailId);
            if (detail == null) {
                log.warn("OrderDetail không tồn tại với ID: {}", orderDetailId);
                return List.of();
            }
            Product product = detail.getProduct();
            if (product == null) {
                log.warn("Product không tồn tại cho OrderDetail ID: {}", orderDetailId);
                return List.of();
            }
            List<Topping> toppings = product.getToppings();
            if (toppings == null || toppings.isEmpty()) {
                return List.of();
            }
            return toppings.stream()
                    .filter(t -> t != null && !Boolean.TRUE.equals(t.getIsDeleted()))
                    .map(t -> new ProductDTO.ToppingDTO(t.getToppingId(), t.getName(), t.getPrice()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Lỗi khi lấy toppings cho OrderDetail {}: {}", orderDetailId, e.getMessage(), e);
            return List.of();
        }
    }

    // Xác nhận thanh toán COD cho đơn hàng
    @PostMapping("/{orderId}/confirm-cod-payment")
    @ResponseBody
    public ResponseEntity<?> confirmCODPayment(
            @PathVariable Integer orderId,
            @RequestParam Integer paymentId,
            HttpSession session) {
        try {
            log.info("Xác nhận thanh toán COD: orderId={}, paymentId={}", orderId, paymentId);
            
            // Lấy account từ session
            Account loggedInUser = (Account) session.getAttribute("loggedInUser");
            Integer accountId = loggedInUser != null ? loggedInUser.getUserId() : 1;
            
            paymentEntityService.confirmCODPayment(paymentId, accountId);
            
            // Reload order và broadcast update
            Order updatedOrder = orderService.getOrderWithDetails(orderId);
            OrderResponseDTO dto = dtoService.convertToOrderDTO(updatedOrder);
            broadcastUpdate(dto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã xác nhận thanh toán COD thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi xác nhận thanh toán COD: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}