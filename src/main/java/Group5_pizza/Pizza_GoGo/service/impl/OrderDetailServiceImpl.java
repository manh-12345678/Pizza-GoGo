package Group5_pizza.Pizza_GoGo.service.impl;

import java.math.BigDecimal;
import java.util.List;

import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.OrderDetail;
import Group5_pizza.Pizza_GoGo.model.Product;
import Group5_pizza.Pizza_GoGo.repository.OrderDetailRepository;
import Group5_pizza.Pizza_GoGo.service.OrderDetailService;
import org.springframework.stereotype.Service;

import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.OrderDetail;
import Group5_pizza.Pizza_GoGo.model.Product;
import Group5_pizza.Pizza_GoGo.repository.OrderDetailRepository;
import Group5_pizza.Pizza_GoGo.service.OrderDetailService;

@Service
public class OrderDetailServiceImpl implements OrderDetailService {

    private final OrderDetailRepository orderDetailRepository;

    public OrderDetailServiceImpl(OrderDetailRepository orderDetailRepository) {
        this.orderDetailRepository = orderDetailRepository;
    }

    @Override
    public OrderDetail addOrUpdateOrderDetail(Order order, Product product, Integer quantity) {
        OrderDetail od = orderDetailRepository
                .findByOrderOrderIdAndProductProductIdAndIsDeletedFalse(order.getOrderId(), product.getProductId())
                .orElse(new OrderDetail());

        od.setOrder(order);
        od.setProduct(product);
        od.setQuantity(quantity);
        od.setUnitPrice(product.getPrice());
        od.setDiscount(java.math.BigDecimal.ZERO);

        return orderDetailRepository.save(od);
    }

    public OrderDetail addOrUpdateOrderDetail(Order order, Product product, Integer quantity, String note) {
        OrderDetail od = orderDetailRepository
                .findByOrderOrderIdAndProductProductIdAndIsDeletedFalse(order.getOrderId(), product.getProductId())
                .orElse(new OrderDetail());

        od.setOrder(order);
        od.setProduct(product);
        od.setQuantity(quantity);
        od.setUnitPrice(product.getPrice());
        od.setDiscount(BigDecimal.ZERO);
        // od.setNote(note);

        return orderDetailRepository.save(od);
    }

    @Override
    public List<OrderDetail> getOrderDetailsByOrder(Order order) {
        return orderDetailRepository.findByOrderAndIsDeletedFalse(order);
    }

    @Override
    public void deleteOrderDetail(Integer orderDetailId) {
        OrderDetail od = getOrderDetailById(orderDetailId);
        od.setIsDeleted(true);
        orderDetailRepository.save(od);
    }

    @Override
    public OrderDetail getOrderDetailById(Integer orderDetailId) {
        return orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new RuntimeException("OrderDetail not found"));
    }
}
