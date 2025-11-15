// src/main/java/Group5_pizza/Pizza_GoGo/service/OrderDetailService.java
package Group5_pizza.Pizza_GoGo.service;

import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.OrderDetail;
import Group5_pizza.Pizza_GoGo.model.Product;
import Group5_pizza.Pizza_GoGo.model.Topping;

import java.util.List;

public interface OrderDetailService {

    /**
     * Thêm hoặc cập nhật OrderDetail (nếu sản phẩm đã tồn tại thì tăng số lượng)
     */
    OrderDetail addOrUpdateOrderDetail(Order order, Product product, Integer quantity);

    OrderDetail addOrUpdateOrderDetail(Order order, Product product, Integer quantity, String note);

    OrderDetail addOrUpdateOrderDetail(Order order, Product product, Integer quantity, String note, Topping topping);
    /**
     * Lấy danh sách OrderDetail chưa bị xóa của một đơn hàng
     */
    List<OrderDetail> getOrderDetailsByOrder(Order order);

    /**
     * Xóa mềm (soft delete) một món trong đơn hàng
     */
    void deleteOrderDetail(Long orderId, Integer orderDetailId);

    /**
     * Lấy OrderDetail theo ID
     */
    OrderDetail getOrderDetailById(Integer orderDetailId);
}