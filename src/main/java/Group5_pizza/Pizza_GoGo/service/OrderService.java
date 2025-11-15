// package Group5_pizza.Pizza_GoGo.service;
// OrderService.java
package Group5_pizza.Pizza_GoGo.service;
import java.util.List;
import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.OrderDetail;
import Group5_pizza.Pizza_GoGo.model.RestaurantTable;
public interface OrderService {
    Order getOrCreatePendingOrderByTable(RestaurantTable table);
    Order getOrderById(Integer orderId);
    List<Order> getAllOrders();
    Order getOrderWithDetails(Integer orderId);
    List<Order> getOrdersByStatus(String status);
    List<Order> getOrdersByStatusWithDetails(String status);
    boolean updateOrderStatus(Integer orderId, String status);
    Order getLatestOrderByTable(RestaurantTable table);
    Order createNewOrderForTable(RestaurantTable table);
    OrderDetail addProductToOrder(Integer orderId, Integer productId, Integer quantity, String note);
    OrderDetail addToppingToOrderDetail(Integer orderDetailId, Integer toppingId);
    Order createOrderForTable(Integer tableId);
    boolean canCancelOrder(Integer orderId);
    boolean cancelOrderByCustomer(Integer orderId);
    List<Order> searchOrders(Integer orderId, Integer tableNumber);
    // Thêm method xóa topping
    void deleteToppingFromOrder(Integer orderDetailToppingId);
    Order saveOrder(Order order);
    // Lấy đơn hàng theo customer ID
    List<Order> getOrdersByCustomerId(Integer customerId);
    // Lấy đơn hàng theo UserId (Account)
    List<Order> getOrdersByUserId(Integer userId);
}