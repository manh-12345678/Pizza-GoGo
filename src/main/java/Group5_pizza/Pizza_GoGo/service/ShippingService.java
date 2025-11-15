package Group5_pizza.Pizza_GoGo.service;

import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.Shipping;

import java.util.List;

public interface ShippingService {
    Shipping createShipping(Order order, String address, String contactName, String contactPhone);
    
    Shipping getShippingByOrderId(Integer orderId);
    
    List<Shipping> getShippingsByOrderId(Integer orderId);
    
    Shipping getShippingById(Integer shippingId);
    
    List<Shipping> getAllShippings();
    
    List<Shipping> getShippingsByStatus(String status);
    
    Shipping updateShippingStatus(Integer shippingId, String status, String shipperName, String shipperPhone);
    
    Shipping assignShipper(Integer shippingId, String shipperName, String shipperPhone);
    
    boolean markAsDelivered(Integer shippingId);
}

