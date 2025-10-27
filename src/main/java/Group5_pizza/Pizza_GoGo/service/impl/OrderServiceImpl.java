package Group5_pizza.Pizza_GoGo.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.RestaurantTable;
import Group5_pizza.Pizza_GoGo.repository.OrderRepository;
import Group5_pizza.Pizza_GoGo.service.OrderService;
import org.springframework.stereotype.Service;

import Group5_pizza.Pizza_GoGo.model.Order;
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
    public Order getOrCreatePendingOrderByTable(RestaurantTable table) {
        return orderRepository.findByTableAndStatusAndIsDeletedFalse(table, "Pending")
                .orElseGet(() -> {
                    Order order = new Order();
                    order.setTable(table);
                    order.setOrderType("Dine-in");
                    order.setTotalAmount(java.math.BigDecimal.ZERO);
                    order.setDepositAmount(java.math.BigDecimal.ZERO);
                    order.setPaidAmount(java.math.BigDecimal.ZERO);
                    order.setStatus("Pending");
                    order.setIsDeleted(false);
                    return orderRepository.save(order);
                });
    }

    @Override
    public Order getOrderById(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // @Override
    // public Order getOrderWithDetails(Integer orderId) {
    // return orderRepository.findByOrderIdAndIsDeletedFalse(orderId)
    // .orElseThrow(() -> new RuntimeException("Order not found"));
    // }

    @Override
    public Order getOrderWithDetails(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setTotalAmount(order.getOrderDetails().stream()
                .filter(od -> od.getIsDeleted() != null && !od.getIsDeleted())
                .map(od -> od.getUnitPrice().multiply(java.math.BigDecimal.valueOf(od.getQuantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        return order;
    }

    @Override
    public List<Order> getOrderByStatus(String status) {
        if (status == null || status.isEmpty()) {
            return orderRepository.findAll();
        }
        return orderRepository.findByStatus(status);
    }

    @Override
    public boolean updateOrderStatus (Integer orderId, String status) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);

        if (optionalOrder.isEmpty()) {
            return false;
        }

        Order order = optionalOrder.get();
        order.setStatus(status);
        orderRepository.save(order);
        return true;
    }

    @Override
    public Order getLatestOrderByTable(RestaurantTable table) {
        return orderRepository.findTopByTableOrderByCreatedAtDesc(table).orElse(null);
    }

    @Override
    public Order createNewOrderForTable(RestaurantTable table) {
        Order order = new Order();
        order.setTable(table);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

}
