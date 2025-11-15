// src/main/java/Group5_pizza/Pizza_GoGo/service/ToppingService.java
package Group5_pizza.Pizza_GoGo.service;

import Group5_pizza.Pizza_GoGo.DTO.ToppingDTO;
import Group5_pizza.Pizza_GoGo.model.Topping;
import java.util.List;

public interface ToppingService {

    List<ToppingDTO> searchToppings(String search);

    ToppingDTO getToppingDTOById(Integer id);

    Topping getToppingById(Integer id);

    // THÊM MỚI: Tìm topping theo tên (dùng trong OrderController)
    Topping getToppingByName(String name);

    Topping createToppingWithIngredients(ToppingDTO toppingDTO);

    Topping updateToppingWithIngredients(Integer id, ToppingDTO toppingDTO);

    void deleteTopping(Integer id);

    List<Topping> getAllToppings();
}