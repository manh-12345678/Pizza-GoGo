package Group5_pizza.Pizza_GoGo.service;

import Group5_pizza.Pizza_GoGo.model.ProductTopping;
import java.util.List;

public interface ProductToppingService {
    List<ProductTopping> getByProductId(Integer productId);
    void saveProductToppings(Integer productId, List<Integer> toppingIds);
    void deleteByProductId(Integer productId);
}