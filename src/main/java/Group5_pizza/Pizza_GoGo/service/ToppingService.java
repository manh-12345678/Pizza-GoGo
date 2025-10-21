package Group5_pizza.Pizza_GoGo.service;

import java.util.List;

import Group5_pizza.Pizza_GoGo.model.Topping;

public interface ToppingService {
    List<Topping> getAllToppings();
    List<Topping> getAllAvailableToppings();
    Topping getToppingById(Integer id);
    Topping saveTopping(Topping topping);
    void deleteTopping(Integer id);
    List<Topping> searchToppings(String name);
}
