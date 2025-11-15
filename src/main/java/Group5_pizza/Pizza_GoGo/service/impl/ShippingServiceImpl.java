package Group5_pizza.Pizza_GoGo.service.impl;

import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.Shipping;
import Group5_pizza.Pizza_GoGo.repository.OrderRepository;
import Group5_pizza.Pizza_GoGo.repository.ShippingRepository;
import Group5_pizza.Pizza_GoGo.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingServiceImpl implements ShippingService {

    private final ShippingRepository shippingRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public Shipping createShipping(Order order, String address, String contactName, String contactPhone) {
        Shipping shipping = new Shipping();
        shipping.setOrder(order);
        shipping.setAddress(address);
        shipping.setContactName(contactName);
        shipping.setContactPhone(contactPhone);
        shipping.setStatus("PENDING");
        shipping.setCreatedAt(LocalDateTime.now());
        return shippingRepository.save(shipping);
    }

    @Override
    public Shipping getShippingByOrderId(Integer orderId) {
        return shippingRepository.findShippingByOrderId(orderId)
                .orElse(null);
    }

    @Override
    public List<Shipping> getShippingsByOrderId(Integer orderId) {
        return shippingRepository.findShippingsByOrderId(orderId);
    }

    @Override
    public Shipping getShippingById(Integer shippingId) {
        return shippingRepository.findById(shippingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy shipping với ID: " + shippingId));
    }

    @Override
    public List<Shipping> getAllShippings() {
        return shippingRepository.findAll();
    }

    @Override
    public List<Shipping> getShippingsByStatus(String status) {
        return shippingRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Override
    @Transactional
    public Shipping updateShippingStatus(Integer shippingId, String status, String shipperName, String shipperPhone) {
        Shipping shipping = getShippingById(shippingId);
        // Normalize status to uppercase để đảm bảo consistency
        if (status != null) {
            shipping.setStatus(status.toUpperCase().trim());
        } else {
            shipping.setStatus("PENDING");
        }
        shipping.setUpdatedAt(LocalDateTime.now());

        if (shipperName != null && !shipperName.trim().isEmpty()) {
            shipping.setShipperName(shipperName);
        }
        if (shipperPhone != null && !shipperPhone.trim().isEmpty()) {
            shipping.setShipperPhone(shipperPhone);
        }

        // Nếu status là DELIVERED, set deliveredAt
        String normalizedStatus = shipping.getStatus();
        if ("DELIVERED".equalsIgnoreCase(normalizedStatus)) {
            shipping.setDeliveredAt(LocalDateTime.now());
        }

        // Cập nhật order status nếu cần
        Order order = shipping.getOrder();
        if (order != null) {
            if ("IN_TRANSIT".equalsIgnoreCase(normalizedStatus) || "OUT_FOR_DELIVERY".equalsIgnoreCase(normalizedStatus)) {
                order.setStatus("PROCESSING");
            } else if ("DELIVERED".equalsIgnoreCase(normalizedStatus)) {
                order.setStatus("COMPLETED");
            }
            orderRepository.save(order);
        }

        return shippingRepository.save(shipping);
    }

    @Override
    @Transactional
    public Shipping assignShipper(Integer shippingId, String shipperName, String shipperPhone) {
        Shipping shipping = getShippingById(shippingId);
        shipping.setShipperName(shipperName);
        shipping.setShipperPhone(shipperPhone);
        shipping.setStatus("ASSIGNED");
        shipping.setUpdatedAt(LocalDateTime.now());
        return shippingRepository.save(shipping);
    }

    @Override
    @Transactional
    public boolean markAsDelivered(Integer shippingId) {
        Shipping shipping = getShippingById(shippingId);
        shipping.setStatus("DELIVERED");
        shipping.setDeliveredAt(LocalDateTime.now());
        shipping.setUpdatedAt(LocalDateTime.now());

        // Cập nhật order status
        Order order = shipping.getOrder();
        if (order != null) {
            order.setStatus("Completed");
            orderRepository.save(order);
        }

        shippingRepository.save(shipping);
        return true;
    }
}

