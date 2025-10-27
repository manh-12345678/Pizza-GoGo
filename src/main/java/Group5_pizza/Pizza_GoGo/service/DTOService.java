package Group5_pizza.Pizza_GoGo.service;

import Group5_pizza.Pizza_GoGo.DTO.OrderDTO;
import Group5_pizza.Pizza_GoGo.model.Order;

public interface DTOService {
    OrderDTO convertToOrderDTO(Order order);
}
