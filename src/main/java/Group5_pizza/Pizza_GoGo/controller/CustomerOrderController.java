package Group5_pizza.Pizza_GoGo.controller;

import Group5_pizza.Pizza_GoGo.model.Account;
import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.Shipping;
import Group5_pizza.Pizza_GoGo.service.OrderService;
import Group5_pizza.Pizza_GoGo.service.ShippingService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class CustomerOrderController {

    private final OrderService orderService;
    private final ShippingService shippingService;

    @GetMapping("/my-orders")
    public String viewMyOrders(HttpSession session, Model model) {
        Account loggedInUser = (Account) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // Lấy đơn hàng theo UserId từ session (query trực tiếp qua accountId)
        List<Order> orders = orderService.getOrdersByUserId(loggedInUser.getUserId());
        
        model.addAttribute("orders", orders);
        model.addAttribute("loggedInUser", loggedInUser);
        
        if (orders.isEmpty()) {
            model.addAttribute("message", "Bạn chưa có đơn hàng nào.");
        }
        
        return "orders/my_orders";
    }

    @GetMapping("/{orderId}")
    public String viewOrderDetail(@PathVariable Integer orderId, HttpSession session, Model model) {
        Account loggedInUser = (Account) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Order order = orderService.getOrderWithDetails(orderId);
        if (order == null) {
            return "redirect:/orders/my-orders";
        }

        // DEBUG: In ra toàn bộ object data
        System.out.println("=== ORDER DETAIL DATA ===");
        System.out.println("Order ID: " + order.getOrderId());
        System.out.println("Order Type: " + order.getOrderType());
        System.out.println("Status: " + order.getStatus());
        System.out.println("Total Amount: " + order.getTotalAmount());
        System.out.println("Paid Amount: " + order.getPaidAmount());
        System.out.println("Deposit Amount: " + order.getDepositAmount());
        System.out.println("Created At: " + order.getCreatedAt());
        System.out.println("Updated At: " + order.getUpdatedAt());
        System.out.println("Is Deleted: " + order.getIsDeleted());
        
        if (order.getTable() != null) {
            System.out.println("Table ID: " + order.getTable().getTableId());
            System.out.println("Table Number: " + order.getTable().getTableNumber());
            System.out.println("Table Name: " + order.getTable().getTableName());
        } else {
            System.out.println("Table: null");
        }
        
        if (order.getAccount() != null) {
            System.out.println("Account ID: " + order.getAccount().getUserId());
            System.out.println("Account Username: " + order.getAccount().getUsername());
        } else {
            System.out.println("Account: null");
        }
        
        if (order.getCustomer() != null) {
            System.out.println("Customer ID: " + order.getCustomer().getCustomerId());
        } else {
            System.out.println("Customer: null");
        }
        
        System.out.println("Number of OrderDetails: " + (order.getOrderDetails() != null ? order.getOrderDetails().size() : 0));
        
        // Debug từng OrderDetail
        if (order.getOrderDetails() != null) {
            int detailIndex = 1;
            for (Group5_pizza.Pizza_GoGo.model.OrderDetail detail : order.getOrderDetails()) {
                if (detail != null && (detail.getIsDeleted() == null || !detail.getIsDeleted())) {
                    System.out.println("\n--- OrderDetail #" + detailIndex + " ---");
                    System.out.println("  OrderDetail ID: " + detail.getOrderDetailId());
                    System.out.println("  Product ID: " + (detail.getProduct() != null ? detail.getProduct().getProductId() : "null"));
                    System.out.println("  Product Name: " + (detail.getProduct() != null ? detail.getProduct().getName() : "null"));
                    System.out.println("  Product Price: " + (detail.getProduct() != null ? detail.getProduct().getPrice() : "null"));
                    System.out.println("  Quantity: " + detail.getQuantity());
                    System.out.println("  Unit Price: " + detail.getUnitPrice());
                    System.out.println("  Discount: " + detail.getDiscount());
                    System.out.println("  Note: " + detail.getNote());
                    System.out.println("  Subtotal (product + toppings): " + detail.getSubtotal());
                    System.out.println("  Is Deleted: " + detail.getIsDeleted());
                    System.out.println("  Created At: " + detail.getCreatedAt());
                    
                    // Debug toppings
                    if (detail.getOrderDetailToppings() != null && !detail.getOrderDetailToppings().isEmpty()) {
                        System.out.println("  Number of Toppings: " + detail.getOrderDetailToppings().size());
                        int toppingIndex = 1;
                        for (Group5_pizza.Pizza_GoGo.model.OrderDetailTopping odt : detail.getOrderDetailToppings()) {
                            if (odt != null && (odt.getIsDeleted() == null || !odt.getIsDeleted()) && odt.getTopping() != null) {
                                System.out.println("    Topping #" + toppingIndex + ":");
                                System.out.println("      OrderDetailTopping ID: " + odt.getOrderDetailToppingId());
                                System.out.println("      Topping ID: " + odt.getTopping().getToppingId());
                                System.out.println("      Topping Name: " + odt.getTopping().getName());
                                System.out.println("      Topping Price (from topping): " + odt.getTopping().getPrice());
                                System.out.println("      Price (stored in OrderDetailTopping): " + odt.getPrice());
                                System.out.println("      Is Deleted: " + odt.getIsDeleted());
                                System.out.println("      Created At: " + odt.getCreatedAt());
                                toppingIndex++;
                            }
                        }
                    } else {
                        System.out.println("  Toppings: none");
                    }
                    detailIndex++;
                }
            }
        }
        
        System.out.println("\n=== END ORDER DETAIL DATA ===\n");

        // Kiểm tra quyền xem đơn hàng (chỉ owner hoặc admin/staff)
        String userRole = (String) session.getAttribute("loggedInUserRole");
        boolean isAdminOrStaff = "ADMIN".equalsIgnoreCase(userRole) || "STAFF".equalsIgnoreCase(userRole);
        
        if (!isAdminOrStaff) {
            // Nếu không phải admin/staff, chỉ cho xem đơn hàng của chính mình
            // Kiểm tra qua accountId (ưu tiên) hoặc customerId (tương thích ngược)
            boolean isOwner = false;
            if (order.getAccount() != null && loggedInUser.getUserId() != null) {
                // Kiểm tra qua accountId
                isOwner = order.getAccount().getUserId().equals(loggedInUser.getUserId());
            } else if (order.getCustomer() != null && loggedInUser.getCustomer() != null) {
                // Fallback: kiểm tra qua customerId (tương thích với dữ liệu cũ)
                isOwner = order.getCustomer().getCustomerId().equals(loggedInUser.getCustomer().getCustomerId());
            }
            
            if (!isOwner) {
                return "redirect:/orders/my-orders";
            }
        }

        // Lấy shipping info nếu có - luôn load mới nhất từ database
        Shipping shipping = null;
        if ("DELIVERY".equalsIgnoreCase(order.getOrderType()) || "ONLINE".equalsIgnoreCase(order.getOrderType())) {
            try {
                List<Shipping> shippings = shippingService.getShippingsByOrderId(orderId);
                if (shippings != null && !shippings.isEmpty()) {
                    // Lấy shipping mới nhất (theo createdAt hoặc updatedAt)
                    shipping = shippings.stream()
                            .max((s1, s2) -> {
                                LocalDateTime t1 = s2.getUpdatedAt() != null ? s2.getUpdatedAt() : s2.getCreatedAt();
                                LocalDateTime t2 = s1.getUpdatedAt() != null ? s1.getUpdatedAt() : s1.getCreatedAt();
                                if (t1 == null && t2 == null) return 0;
                                if (t1 == null) return -1;
                                if (t2 == null) return 1;
                                return t1.compareTo(t2);
                            })
                            .orElse(shippings.get(0));
                    log.debug("Loaded shipping for order {}: status={}, shipper={}", 
                            orderId, shipping.getStatus(), shipping.getShipperName());
                } else {
                    log.debug("No shipping found for order {}", orderId);
                }
            } catch (Exception e) {
                log.warn("Không tìm thấy shipping cho order {}: {}", orderId, e.getMessage(), e);
            }
        }

        model.addAttribute("order", order);
        model.addAttribute("shipping", shipping);
        model.addAttribute("loggedInUser", loggedInUser);
        return "orders/order_detail";
    }
}

