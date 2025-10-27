package Group5_pizza.Pizza_GoGo.service;

import java.util.List;

import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.RestaurantTable;

public interface OrderService {
    Order getOrCreatePendingOrderByTable(RestaurantTable table);

    Order getOrderById(Integer orderId); // Phương thức lấy order cơ bản

    List<Order> getAllOrders();

    Order getOrderWithDetails(Integer orderId); // Lấy 1 order với chi tiết (đã sửa)

    List<Order> getOrdersByStatus(String status); // Lấy danh sách order cơ bản theo status

    // Phương thức lấy danh sách order kèm chi tiết cho Admin
    List<Order> getOrdersByStatusWithDetails(String status);

    boolean updateOrderStatus(Integer orderId, String status);

    Order getLatestOrderByTable(RestaurantTable table);

    Order createNewOrderForTable(RestaurantTable table);
}
