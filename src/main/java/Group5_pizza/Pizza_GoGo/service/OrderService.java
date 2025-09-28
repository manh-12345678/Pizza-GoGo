package Group5_pizza.Pizza_GoGo.service;

import java.util.List;

import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.RestaurantTable;

public interface OrderService {
    Order getOrCreatePendingOrderByTable(RestaurantTable table);

    Order getOrderById(Integer orderId);

    List<Order> getAllOrders();

    Order getOrderWithDetails(Integer orderId);
}
