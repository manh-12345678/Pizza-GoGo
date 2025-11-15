// src/main/java/Group5_pizza/Pizza_GoGo/service/impl/OrderDetailServiceImpl.java
package Group5_pizza.Pizza_GoGo.service.impl;

import Group5_pizza.Pizza_GoGo.model.*;
import Group5_pizza.Pizza_GoGo.repository.OrderDetailRepository;
import Group5_pizza.Pizza_GoGo.repository.OrderDetailToppingRepository;
import Group5_pizza.Pizza_GoGo.repository.OrderRepository;
import Group5_pizza.Pizza_GoGo.service.OrderDetailService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderDetailServiceImpl implements OrderDetailService {

    private final OrderRepository orderRepo;
    private final OrderDetailRepository orderDetailRepository;
    private final OrderDetailToppingRepository orderDetailToppingRepository;

    public OrderDetailServiceImpl(
            OrderRepository orderRepo,
            OrderDetailRepository orderDetailRepository,
            OrderDetailToppingRepository orderDetailToppingRepository) {
        this.orderRepo = orderRepo;
        this.orderDetailRepository = orderDetailRepository;
        this.orderDetailToppingRepository = orderDetailToppingRepository;
    }

    @Override
    public OrderDetail addOrUpdateOrderDetail(Order order, Product product, Integer quantity) {
        return addOrUpdateOrderDetail(order, product, quantity, null, null);
    }

    @Override
    public OrderDetail addOrUpdateOrderDetail(Order order, Product product, Integer quantity, String note) {
        return addOrUpdateOrderDetail(order, product, quantity, note, null);
    }

    @Override
    @Transactional
    public OrderDetail addOrUpdateOrderDetail(Order order, Product product, Integer quantity, String note, Topping topping) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }

        // Tìm OrderDetail hiện có (cùng sản phẩm + chưa xóa)
        OrderDetail od = orderDetailRepository
                .findByOrderOrderIdAndProductProductIdAndIsDeletedFalse(order.getOrderId(), product.getProductId())
                .orElse(new OrderDetail());

        boolean isNew = od.getOrderDetailId() == null;

        // Cập nhật thông tin
        od.setOrder(order);
        od.setProduct(product);
        od.setQuantity(isNew ? quantity : od.getQuantity() + quantity);
        od.setUnitPrice(product.getPrice());
        od.setDiscount(BigDecimal.ZERO);
        od.setNote(note);
        od.setIsDeleted(false);

        OrderDetail saved = orderDetailRepository.save(od);

        // Thêm topping nếu có
        if (topping != null) {
            OrderDetailTopping odt = new OrderDetailTopping();
            odt.setOrderDetail(saved);
            odt.setTopping(topping);
            odt.setIsDeleted(false);
            saved.getOrderDetailToppings().add(odt);
            orderDetailToppingRepository.save(odt);
        }

        recalculateTotalAmount(order);
        return saved;
    }

    @Override
    public List<OrderDetail> getOrderDetailsByOrder(Order order) {
        return orderDetailRepository.findByOrderAndIsDeletedFalse(order);
    }

    @Override
    @Transactional
    public void deleteOrderDetail(Long orderId, Integer orderDetailId) {
        Order order = orderRepo.findById(Math.toIntExact(orderId))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));

        if (orderDetailId == null) {
            throw new IllegalArgumentException("OrderDetail ID không được để trống");
        }
        OrderDetail od = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món với ID: " + orderDetailId));

        if (od.getIsDeleted()) {
            return;
        }

        od.setIsDeleted(true);
        orderDetailRepository.save(od);

        recalculateTotalAmount(order);
    }

    @Override
    public OrderDetail getOrderDetailById(Integer orderDetailId) {
        if (orderDetailId == null) {
            throw new IllegalArgumentException("OrderDetail ID không được để trống");
        }
        return orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy OrderDetail với ID: " + orderDetailId));
    }

    // Tính lại tổng tiền đơn hàng (bao gồm món + topping)
    private void recalculateTotalAmount(Order order) {
        BigDecimal total = BigDecimal.ZERO;

        List<OrderDetail> details = orderDetailRepository.findByOrderAndIsDeletedFalse(order);
        for (OrderDetail od : details) {
            BigDecimal subtotal = od.getUnitPrice()
                    .multiply(BigDecimal.valueOf(od.getQuantity()))
                    .subtract(od.getDiscount() != null ? od.getDiscount() : BigDecimal.ZERO);
            total = total.add(subtotal);

            // Cộng tiền topping
            for (OrderDetailTopping odt : od.getOrderDetailToppings()) {
                if (odt.getIsDeleted() == null || !odt.getIsDeleted()) {
                    total = total.add(odt.getTopping().getPrice());
                }
            }
        }

        order.setTotalAmount(total);
        orderRepo.save(order);
    }
}