// package Group5_pizza.Pizza_GoGo.service.impl;
// OrderServiceImpl.java
package Group5_pizza.Pizza_GoGo.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Group5_pizza.Pizza_GoGo.DTO.OrderResponseDTO;
import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.OrderDetail;
import Group5_pizza.Pizza_GoGo.model.OrderDetailTopping;
import Group5_pizza.Pizza_GoGo.model.Product;
import Group5_pizza.Pizza_GoGo.model.RestaurantTable;
import Group5_pizza.Pizza_GoGo.model.Topping;
import Group5_pizza.Pizza_GoGo.repository.OrderDetailRepository;
import Group5_pizza.Pizza_GoGo.repository.OrderDetailToppingRepository;
import Group5_pizza.Pizza_GoGo.repository.OrderRepository;
import Group5_pizza.Pizza_GoGo.repository.ProductRepository;
import Group5_pizza.Pizza_GoGo.repository.RestaurantTableRepository;
import Group5_pizza.Pizza_GoGo.repository.ToppingRepository;
import Group5_pizza.Pizza_GoGo.service.DTOService;
import Group5_pizza.Pizza_GoGo.service.OrderService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepo;
    private final OrderDetailRepository detailRepo;
    private final OrderDetailToppingRepository toppingRepo;
    private final ProductRepository productRepo;
    private final ToppingRepository toppingRepository;
    private final RestaurantTableRepository tableRepo;
    private final SimpMessagingTemplate messagingTemplate;
    private final DTOService dtoService; // ❗ THÊM: Inject DTOService để send DTO
    // 1. Lấy hoặc tạo đơn PENDING cho bàn

    @Override
    @Transactional
    public Order getOrCreatePendingOrderByTable(RestaurantTable table) {
        return orderRepo.findByTableAndStatusAndIsDeletedFalse(table, "PENDING")
                .orElseGet(() -> createNewOrderForTable(table));
    }

    // 2. Lấy đơn theo ID (có chi tiết)
    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Integer orderId) {
        return orderRepo.findByOrderIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn không tồn tại hoặc đã bị xóa"));
    }

    // 3. Lấy tất cả đơn
    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        List<Order> orders = orderRepo.findByIsDeletedFalse();
        // Sắp xếp sau khi load: PENDING trước, NULL tiếp theo, sau đó các status khác
        orders.sort((o1, o2) -> {
            int priority1 = getStatusPriority(o1.getStatus());
            int priority2 = getStatusPriority(o2.getStatus());
            if (priority1 != priority2) {
                return Integer.compare(priority1, priority2);
            }
            // Nếu cùng priority, sắp xếp theo createdAt DESC
            if (o1.getCreatedAt() != null && o2.getCreatedAt() != null) {
                return o2.getCreatedAt().compareTo(o1.getCreatedAt());
            }
            return 0;
        });
        return orders;
    }

    private int getStatusPriority(String status) {
        if (status == null) {
            return 1; // NULL có priority 1
        }
        if ("PENDING".equals(status)) {
            return 0; // PENDING có priority 0 (cao nhất)
        }
        return 2; // Các status khác có priority 2
    }

    // 4. Lấy đơn + chi tiết
    @Override
    @Transactional // Bỏ readOnly để có thể modify collection
    public Order getOrderWithDetails(Integer orderId) {
        // Load Order với OrderDetails và Product (KHÔNG JOIN FETCH toppings để tránh
        // duplicate)
        // Sau đó sẽ load toppings riêng cho từng OrderDetail
        Order order = orderRepo.findByIdWithDetailsOnly(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        System.out.println("=== DEBUG: Loaded Order with " +
                (order.getOrderDetails() != null ? order.getOrderDetails().size() : 0) + " OrderDetails");

        // Load toppings riêng cho từng OrderDetail (vì không JOIN FETCH để tránh
        // duplicate)
        if (order.getOrderDetails() != null) {
            for (OrderDetail detail : order.getOrderDetails()) {
                if (detail != null && detail.getOrderDetailId() != null &&
                        (detail.getIsDeleted() == null || !detail.getIsDeleted())) {
                    System.out.println("Loading toppings for OrderDetail ID: " + detail.getOrderDetailId());

                    // Load toppings từ database
                    try {
                        // Dùng JPQL query với JOIN FETCH để load cả topping
                        List<OrderDetailTopping> toppings = toppingRepo
                                .findByOrderDetailIdWithTopping(detail.getOrderDetailId());
                        System.out.println("  Query executed successfully");
                        System.out.println("  Found " + toppings.size() + " toppings from database");

                        if (toppings.isEmpty()) {
                            System.out.println("  WARNING: No toppings found! Check database for order_detail_id = "
                                    + detail.getOrderDetailId());
                        } else {
                            // Debug từng topping trước khi set
                            System.out.println("  Toppings details from query:");
                            for (OrderDetailTopping odt : toppings) {
                                if (odt != null) {
                                    System.out.println("    - OrderDetailTopping ID: " + odt.getOrderDetailToppingId() +
                                            ", Topping ID: "
                                            + (odt.getTopping() != null ? odt.getTopping().getToppingId() : "null") +
                                            ", Topping Name: "
                                            + (odt.getTopping() != null ? odt.getTopping().getName() : "null") +
                                            ", Price: " + odt.getPrice() +
                                            ", IsDeleted: " + odt.getIsDeleted());
                                } else {
                                    System.out.println("    - NULL topping found!");
                                }
                            }

                            // Set toppings vào OrderDetail
                            // KHÔNG được set Set mới (sẽ trigger orphanRemoval)
                            // Cũng không nên clear() vì có thể gây vấn đề với PersistentSet
                            // Thay vào đó, remove các topping không còn trong danh sách mới
                            // và add các topping mới

                            if (detail.getOrderDetailToppings() == null) {
                                detail.setOrderDetailToppings(new java.util.HashSet<>());
                            }

                            // Tạo Set các topping ID cần giữ lại
                            java.util.Set<Integer> newToppingIds = new java.util.HashSet<>();
                            for (OrderDetailTopping odt : toppings) {
                                if (odt != null && odt.getOrderDetailToppingId() != null) {
                                    newToppingIds.add(odt.getOrderDetailToppingId());
                                }
                            }

                            // Remove các topping không còn trong danh sách mới
                            java.util.Iterator<OrderDetailTopping> iterator = detail.getOrderDetailToppings()
                                    .iterator();
                            while (iterator.hasNext()) {
                                OrderDetailTopping existing = iterator.next();
                                if (existing != null && existing.getOrderDetailToppingId() != null) {
                                    if (!newToppingIds.contains(existing.getOrderDetailToppingId())) {
                                        iterator.remove();
                                    }
                                }
                            }

                            // Add các topping mới vào collection hiện có
                            for (OrderDetailTopping odt : toppings) {
                                if (odt != null) {
                                    // Đảm bảo orderDetail reference được set đúng
                                    odt.setOrderDetail(detail);
                                    // Chỉ add nếu chưa có trong collection
                                    if (!detail.getOrderDetailToppings().contains(odt)) {
                                        detail.getOrderDetailToppings().add(odt);
                                    }
                                }
                            }

                            System.out.println("  Toppings set to OrderDetail successfully");
                        }

                        System.out.println("  Final toppings count in OrderDetail: " +
                                (detail.getOrderDetailToppings() != null ? detail.getOrderDetailToppings().size() : 0));
                    } catch (Exception e) {
                        System.out.println("  ERROR loading toppings: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            // Tính lại tổng tiền của order (bao gồm cả toppings)
            order.recalculateTotal();
            System.out.println("=== DEBUG: Final total amount: " + order.getTotalAmount());
        }

        return order;
    }

    // 5. Lấy đơn theo trạng thái
    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(String status) {
        return orderRepo.findByStatusAndIsDeletedFalse(status.toUpperCase());
    }

    // 6. Lấy đơn theo trạng thái + chi tiết
    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatusWithDetails(String status) {
        List<Order> orders = status == null || status.isEmpty()
                ? orderRepo.findByIsDeletedFalse()
                : orderRepo.findByStatusAndIsDeletedFalse(status != null ? status.toUpperCase() : null);

        // Sắp xếp sau khi load: PENDING trước, NULL tiếp theo, sau đó các status khác
        orders.sort((o1, o2) -> {
            int priority1 = getStatusPriority(o1.getStatus());
            int priority2 = getStatusPriority(o2.getStatus());
            if (priority1 != priority2) {
                return Integer.compare(priority1, priority2);
            }
            // Nếu cùng priority, sắp xếp theo createdAt DESC
            if (o1.getCreatedAt() != null && o2.getCreatedAt() != null) {
                return o2.getCreatedAt().compareTo(o1.getCreatedAt());
            }
            return 0;
        });

        // Load payments cho mỗi order để tránh LazyInitializationException
        // Payments sẽ được load khi cần trong DTO
        return orders;
    }

    // 7. Cập nhật trạng thái đơn
    @Override
    @Transactional
    public boolean updateOrderStatus(Integer orderId, String status) {
        Order order = getOrderById(orderId);
        if ("COMPLETED".equals(order.getStatus())) {
            throw new RuntimeException("Không thể cập nhật đơn đã hoàn thành");
        }
        order.setStatus(status.toUpperCase());
        orderRepo.save(order);
        // Gửi real-time update: Send DTO
        OrderResponseDTO orderDTO = dtoService.convertToOrderDTO(order);
        if (orderDTO != null) {
            messagingTemplate.convertAndSend("/topic/orders/update", orderDTO);
        }
        return true;
    }

    // 8. Lấy đơn mới nhất của bàn
    @Override
    @Transactional(readOnly = true)
    public Order getLatestOrderByTable(RestaurantTable table) {
        return orderRepo.findTopByTableAndIsDeletedFalseOrderByCreatedAtDesc(table)
                .orElse(null);
    }

    // 9. Tạo đơn mới cho bàn
    @Override
    @Transactional
    public Order createNewOrderForTable(RestaurantTable table) {
        Order order = Order.builder()
                .table(table)
                .orderType("DINE_IN")
                .status("PENDING")
                .orderDetails(new ArrayList<>()) // Đảm bảo orderDetails được khởi tạo
                .build();
        return orderRepo.save(order);
    }

    // 10. Tạo đơn cho bàn (theo tableId)
    @Override
    @Transactional
    public Order createOrderForTable(Integer tableId) {
        if (tableId == null) {
            throw new IllegalArgumentException("Table ID không được để trống");
        }
        RestaurantTable table = tableRepo.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại"));
        return createNewOrderForTable(table);
    }

    // 11. Thêm sản phẩm vào đơn
    @Override
    @Transactional
    public OrderDetail addProductToOrder(Integer orderId, Integer productId, Integer quantity, String note) {
        Order order = getOrderById(orderId);
        if ("COMPLETED".equals(order.getStatus())) {
            throw new RuntimeException("Không thể thêm món vào đơn đã hoàn thành");
        }
        if (productId == null) {
            throw new IllegalArgumentException("Product ID không được để trống");
        }
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        OrderDetail detail = new OrderDetail();
        detail.setOrder(order);
        detail.setProduct(product);
        detail.setQuantity(quantity);
        detail.setUnitPrice(product.getPrice());
        detail.setNote(note);
        order.addOrderDetail(detail);
        order.recalculateTotal(); // ❗ FIX: Tính lại tổng giá
        orderRepo.save(order);
        // Real-time: Send DTO thay vì entity
        OrderResponseDTO orderDTO = dtoService.convertToOrderDTO(order);
        if (orderDTO != null) {
            messagingTemplate.convertAndSend("/topic/orders/update", orderDTO);
        }
        return detail;
    }

    // 12. Thêm topping vào chi tiết đơn
    @Override
    @Transactional
    public OrderDetail addToppingToOrderDetail(Integer orderDetailId, Integer toppingId) {
        if (orderDetailId == null) {
            throw new IllegalArgumentException("OrderDetail ID không được để trống");
        }
        if (toppingId == null) {
            throw new IllegalArgumentException("Topping ID không được để trống");
        }

        // Load detail với order để kiểm tra status
        OrderDetail detail = detailRepo.findById(orderDetailId)
                .orElseThrow(() -> new RuntimeException("Chi tiết đơn không tồn tại"));

        // Lấy orderId trực tiếp để tránh load toàn bộ order
        Integer orderId = detail.getOrder().getOrderId();

        // Kiểm tra status bằng query riêng để tránh vòng lặp
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order không tồn tại"));

        if ("COMPLETED".equals(order.getStatus())) {
            throw new RuntimeException("Không thể thêm topping vào đơn đã hoàn thành");
        }

        Topping topping = toppingRepository.findById(toppingId)
                .orElseThrow(() -> new RuntimeException("Topping không tồn tại"));

        // Cho phép thêm nhiều lần cùng một topping (ví dụ: double cheese, triple
        // cheese)
        // Không check duplicate để người dùng có thể thêm nhiều lần

        // Tạo OrderDetailTopping mới
        OrderDetailTopping odt = OrderDetailTopping.builder()
                .topping(topping)
                .price(topping.getPrice())
                .isDeleted(false)
                .build();

        // Sử dụng method addTopping() của OrderDetail để đảm bảo bidirectional
        // relationship
        detail.addTopping(odt);

        // Lưu trực tiếp
        toppingRepo.save(odt);
        toppingRepo.flush();

        System.out.println("Topping saved: ID=" + odt.getOrderDetailToppingId() +
                ", Topping=" + (odt.getTopping() != null ? odt.getTopping().getName() : "null") +
                ", Price=" + odt.getPrice());

        // Tính lại total amount của order
        // Load order với details để tính lại
        order = orderRepo.findByIdWithDetailsAndToppings(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.recalculateTotal();
        orderRepo.save(order);

        // Load lại detail với toppings để return
        detail = detailRepo.findByIdWithToppings(orderDetailId)
                .orElseThrow(() -> new RuntimeException("Chi tiết đơn không tồn tại"));

        return detail;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> searchOrders(Integer orderId, Integer tableNumber) {
        List<Order> orders = orderRepo.searchOrders(orderId, tableNumber);
        // Sắp xếp sau khi load: PENDING trước, NULL tiếp theo, sau đó các status khác
        orders.sort((o1, o2) -> {
            int priority1 = getStatusPriority(o1.getStatus());
            int priority2 = getStatusPriority(o2.getStatus());
            if (priority1 != priority2) {
                return Integer.compare(priority1, priority2);
            }
            // Nếu cùng priority, sắp xếp theo createdAt DESC
            if (o1.getCreatedAt() != null && o2.getCreatedAt() != null) {
                return o2.getCreatedAt().compareTo(o1.getCreatedAt());
            }
            return 0;
        });
        return orders;
    }

    @Override
    @Transactional
    public Order saveOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        return orderRepo.save(order);
    }

    // 13. Kiểm tra có thể hủy không (khách hàng)
    @Override
    @Transactional(readOnly = true)
    public boolean canCancelOrder(Integer orderId) {
        Order order = getOrderById(orderId);
        if (!"PENDING".equals(order.getStatus()))
            return false;
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        return order.getOrderDetails().stream()
                .filter(d -> !Boolean.TRUE.equals(d.getIsDeleted()))
                .noneMatch(d -> d.getCreatedAt().isBefore(fiveMinutesAgo));
    }

    // 14. Hủy đơn bởi khách hàng
    @Override
    @Transactional
    public boolean cancelOrderByCustomer(Integer orderId) {
        if (!canCancelOrder(orderId)) {
            throw new RuntimeException("Không thể hủy: Đã quá 5 phút kể từ khi gọi món");
        }
        return updateOrderStatus(orderId, "CANCELLED");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByCustomerId(Integer customerId) {
        if (customerId == null) {
            return List.of();
        }
        return orderRepo.findByCustomerCustomerIdAndIsDeletedFalseOrderByCreatedAtDesc(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserId(Integer userId) {
        if (userId == null) {
            return List.of();
        }
        return orderRepo.findByAccountUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId);
    }

    // deleteToppingFromOrder (fix để tính lại giá)
    @Override
    @Transactional
    public void deleteToppingFromOrder(Integer orderDetailToppingId) {
        if (orderDetailToppingId == null) {
            throw new IllegalArgumentException("OrderDetailTopping ID không được để trống");
        }
        OrderDetailTopping topping = toppingRepo.findById(orderDetailToppingId)
                .orElseThrow(() -> new RuntimeException("Topping không tồn tại"));
        topping.setIsDeleted(true);
        toppingRepo.save(topping);
        // Update giá order
        Order order = topping.getOrderDetail().getOrder();
        order.recalculateTotal();
        orderRepo.save(order);
        // Real-time: Send DTO
        OrderResponseDTO orderDTO = dtoService.convertToOrderDTO(order);
        if (orderDTO != null) {
            messagingTemplate.convertAndSend("/topic/orders/update", orderDTO);
        }
    }
}