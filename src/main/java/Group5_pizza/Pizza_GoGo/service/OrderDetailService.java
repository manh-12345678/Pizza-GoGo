package Group5_pizza.Pizza_GoGo.service;

import java.util.List;

import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.OrderDetail;
import Group5_pizza.Pizza_GoGo.model.Product;

public interface OrderDetailService {
    OrderDetail addOrUpdateOrderDetail(Order order, Product product, Integer quantity);

    List<OrderDetail> getOrderDetailsByOrder(Order order);

    void deleteOrderDetail(Integer orderDetailId);

    OrderDetail getOrderDetailById(Integer orderDetailId);

    OrderDetail addOrUpdateOrderDetail(Order order, Product product, Integer quantity, String note);
}
