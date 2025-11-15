// package Group5_pizza.Pizza_GoGo.service;
// IngredientService.java
package Group5_pizza.Pizza_GoGo.service;
import Group5_pizza.Pizza_GoGo.DTO.IngredientDTO;
import Group5_pizza.Pizza_GoGo.model.Ingredient;
import java.util.List;
public interface IngredientService {
    List<Ingredient> getAllIngredients();
    Ingredient getIngredientById(Integer id);
    Ingredient createIngredient(IngredientDTO ingredientDTO);
    Ingredient updateIngredient(Integer id, IngredientDTO ingredientDTO);
    boolean deleteIngredient(Integer id);
    List<Ingredient> searchIngredients(String name);
    List<Ingredient> getLowStockIngredients();
    IngredientDTO convertToDTO(Ingredient ingredient);
}