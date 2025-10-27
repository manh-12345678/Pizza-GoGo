package Group5_pizza.Pizza_GoGo.service;

import Group5_pizza.Pizza_GoGo.model.Topping;
import java.util.List;

public interface ToppingService {
    List<Topping> getAll();
    Topping getById(Integer id);
    Topping create(Topping topping);
    Topping update(Integer id, Topping topping);
    void softDelete(Integer id);

    List<Topping> searchByName(String name);
}