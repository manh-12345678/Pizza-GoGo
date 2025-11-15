// package Group5_pizza.Pizza_GoGo.service.impl;
// DTOServiceImpl.java
package Group5_pizza.Pizza_GoGo.service.impl;
import Group5_pizza.Pizza_GoGo.DTO.OrderResponseDTO;
import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.Payment;
import Group5_pizza.Pizza_GoGo.model.RestaurantTable;
import Group5_pizza.Pizza_GoGo.repository.PaymentRepository;
import Group5_pizza.Pizza_GoGo.repository.RestaurantTableRepository;
import Group5_pizza.Pizza_GoGo.service.DTOService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DTOServiceImpl implements DTOService {
    private final PaymentRepository paymentRepository;
    private final RestaurantTableRepository tableRepository;
    
    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO convertToOrderDTO(Order order) {
        try {
            // Load payments và table nếu chưa được load (tránh LazyInitializationException)
            if (order != null && order.getOrderId() != null) {
                try {
                    // Load payments nếu chưa được load
                    if (!Hibernate.isInitialized(order.getPayments())) {
                        List<Payment> payments = paymentRepository.findByOrderOrderIdAndIsDeletedFalse(order.getOrderId());
                        if (payments != null && !payments.isEmpty()) {
                            order.getPayments().clear();
                            order.getPayments().addAll(payments);
                        }
                    }
                    // Load table nếu chưa được load (để hiển thị tên bàn trong quản lý đơn hàng)
                    if (order.getTable() != null) {
                        if (!Hibernate.isInitialized(order.getTable())) {
                            // Nếu table chưa được load, load lại từ repository
                            Integer tableId = order.getTable().getTableId();
                            if (tableId != null) {
                                RestaurantTable loadedTable = tableRepository.findById(tableId).orElse(null);
                                if (loadedTable != null) {
                                    order.setTable(loadedTable);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Không thể load payments/table cho Order {}: {}", order.getOrderId(), e.getMessage());
                }
            }
            
            return new OrderResponseDTO(order);
        } catch (Exception e) {
            log.error("Lỗi khi convert Order {} sang DTO: {}", 
                    order != null ? order.getOrderId() : "null", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi convert Order sang DTO: " + e.getMessage(), e);
        }
    }
}
