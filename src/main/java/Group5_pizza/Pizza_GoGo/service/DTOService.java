// package Group5_pizza.Pizza_GoGo.service;
// DTOService.java
package Group5_pizza.Pizza_GoGo.service;
import Group5_pizza.Pizza_GoGo.DTO.OrderResponseDTO;
import Group5_pizza.Pizza_GoGo.model.Order;
public interface DTOService {
    OrderResponseDTO convertToOrderDTO(Order order);
}