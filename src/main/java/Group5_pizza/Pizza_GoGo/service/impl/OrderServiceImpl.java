package Group5_pizza.Pizza_GoGo.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Quan trọng!

import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.OrderDetail;
import Group5_pizza.Pizza_GoGo.model.RestaurantTable;
import Group5_pizza.Pizza_GoGo.repository.OrderRepository;
import Group5_pizza.Pizza_GoGo.service.OrderService;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional // Thêm để đảm bảo create hoạt động đúng
    public Order getOrCreatePendingOrderByTable(RestaurantTable table) {
        // Nên dùng chữ hoa cho Status
        return orderRepository.findByTableAndStatusAndIsDeletedFalse(table, "PENDING")
                .orElseGet(() -> createNewOrderForTable(table));
    }

    // Chỉ lấy thông tin cơ bản
    @Override
    public Order getOrderById(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Tính toán và gán lại totalAmount + orderDetails
     */
    @Override
    @Transactional(readOnly = true) // Cần Transactional để truy cập lazy relations nếu cần
    public Order getOrderWithDetails(Integer orderId) {
        Order order = orderRepository.findByOrderIdAndIsDeletedFalse(orderId) // Gọi hàm có @EntityGraph
                .orElseThrow(() -> new RuntimeException("Order not found or deleted with ID: " + orderId));

        // Lọc item chưa xóa
        List<OrderDetail> filteredDetails = (order.getOrderDetails() != null)
                ? order.getOrderDetails().stream()
                .filter(od -> od.getIsDeleted() == null || !od.getIsDeleted())
                .collect(Collectors.toList())
                : Collections.emptyList();

        // Tính tổng tiền từ danh sách ĐÃ LỌC
        BigDecimal total = filteredDetails.stream()
                .map(od -> (od.getUnitPrice() != null ? od.getUnitPrice() : BigDecimal.ZERO)
                        .multiply(BigDecimal.valueOf(od.getQuantity() != null ? od.getQuantity() : 0)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // GÁN LẠI KẾT QUẢ VÀO ORDER
        order.setOrderDetails(filteredDetails);
        order.setTotalAmount(total);

        return order;
    }

    // Phương thức cũ, lazy loading
    @Override
    public List<Order> getOrdersByStatus(String status) {
        String upperStatus = (status != null && !status.isEmpty()) ? status.toUpperCase() : null;
        if (upperStatus == null || upperStatus.equals("ALL")) {
            return orderRepository.findAll();
        }
        return orderRepository.findByStatus(upperStatus);
    }

    /**
     * THÊM MỚI/ĐÃ SỬA: Phương thức "thông minh" cho Admin Dashboard
     * Tải danh sách Order kèm chi tiết và tính toán lại.
     */
    @Override
    @Transactional(readOnly = true) // Cần Transactional
    public List<Order> getOrdersByStatusWithDetails(String status) {
        List<Order> orders;
        String upperStatus = (status != null && !status.isEmpty()) ? status.toUpperCase() : null;

        // 1. Gọi Repository đã có @EntityGraph
        if (upperStatus == null || upperStatus.equals("ALL")) {
            orders = orderRepository.findByIsDeletedFalse(); // Lấy tất cả chưa xóa
        } else {
            orders = orderRepository.findByStatusAndIsDeletedFalse(upperStatus); // Lấy theo status chưa xóa
        }

        // 2. Lặp qua từng order để lọc item và tính/gán lại tổng tiền
        for (Order order : orders) {
            List<OrderDetail> filteredDetails = (order.getOrderDetails() != null)
                    ? order.getOrderDetails().stream()
                    .filter(od -> od.getIsDeleted() == null || !od.getIsDeleted())
                    .collect(Collectors.toList())
                    : Collections.emptyList();

            BigDecimal total = filteredDetails.stream()
                    .map(od -> (od.getUnitPrice() != null ? od.getUnitPrice() : BigDecimal.ZERO)
                            .multiply(BigDecimal.valueOf(od.getQuantity() != null ? od.getQuantity() : 0)))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // GÁN LẠI KẾT QUẢ VÀO ORDER
            order.setOrderDetails(filteredDetails);
            order.setTotalAmount(total);
        }
        return orders;
    }


    @Override
    @Transactional // Cần Transactional để save
    public boolean updateOrderStatus(Integer orderId, String status) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        // Kiểm tra order có tồn tại và chưa bị xóa
        if (optionalOrder.isEmpty() || (optionalOrder.get().getIsDeleted() != null && optionalOrder.get().getIsDeleted())) {
            return false;
        }
        Order order = optionalOrder.get();
        order.setStatus(status.toUpperCase()); // Chuẩn hóa
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        return true;
    }

    @Override
    public Order getLatestOrderByTable(RestaurantTable table) {
        return orderRepository.findTopByTableOrderByCreatedAtDesc(table).orElse(null);
    }

    @Override
    @Transactional // Cần Transactional để save
    public Order createNewOrderForTable(RestaurantTable table) {
        Order order = new Order();
        order.setTable(table);
        order.setOrderType("Dine-in"); // Hoặc giá trị mặc định
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now()); // Khởi tạo
        order.setTotalAmount(BigDecimal.ZERO);
        order.setDepositAmount(BigDecimal.ZERO);
        order.setPaidAmount(BigDecimal.ZERO);
        order.setIsDeleted(false);
        return orderRepository.save(order);
    }
}
