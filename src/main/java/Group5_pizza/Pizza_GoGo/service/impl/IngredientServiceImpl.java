// package Group5_pizza.Pizza_GoGo.service.impl;
// IngredientServiceImpl.java
package Group5_pizza.Pizza_GoGo.service.impl;
import Group5_pizza.Pizza_GoGo.DTO.IngredientDTO;
import Group5_pizza.Pizza_GoGo.model.Ingredient;
import Group5_pizza.Pizza_GoGo.repository.IngredientRepository;
import Group5_pizza.Pizza_GoGo.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
@Service
@RequiredArgsConstructor
public class IngredientServiceImpl implements IngredientService {
    private final IngredientRepository ingredientRepository;
    @Override
    public List<Ingredient> getAllIngredients() {
        return ingredientRepository.findByIsDeletedFalse();
    }
    @Override
    public Ingredient getIngredientById(Integer id) {
        return ingredientRepository.findByIngredientIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Ingredient not found with id: " + id));
    }
    @Override
    @Transactional
    public Ingredient createIngredient(IngredientDTO ingredientDTO) {
        validateIngredientDTO(ingredientDTO);
        Ingredient ingredient = new Ingredient();
        ingredient.setName(ingredientDTO.getName());
        ingredient.setUnit(ingredientDTO.getUnit());
        ingredient.setStockQuantity(ingredientDTO.getStockQuantity() != null ?
                ingredientDTO.getStockQuantity() : BigDecimal.ZERO);
        ingredient.setUnitPrice(ingredientDTO.getUnitPrice() != null ?
                ingredientDTO.getUnitPrice() : BigDecimal.ZERO);
        ingredient.setMinimumStock(ingredientDTO.getMinimumStock());
        ingredient.setIsDeleted(false);
        return ingredientRepository.save(ingredient);
    }
    @Override
    @Transactional
    public Ingredient updateIngredient(Integer id, IngredientDTO ingredientDTO) {
        validateIngredientDTO(ingredientDTO);

        Ingredient ingredient = getIngredientById(id);
        ingredient.setName(ingredientDTO.getName());
        ingredient.setUnit(ingredientDTO.getUnit());
        ingredient.setStockQuantity(ingredientDTO.getStockQuantity());
        ingredient.setUnitPrice(ingredientDTO.getUnitPrice());
        ingredient.setMinimumStock(ingredientDTO.getMinimumStock());
        return ingredientRepository.save(ingredient);
    }
    @Override
    @Transactional
    public boolean deleteIngredient(Integer id) {
        Ingredient ingredient = getIngredientById(id);
        ingredient.setIsDeleted(true);
        ingredientRepository.save(ingredient);
        return true;
    }
    @Override
    public List<Ingredient> searchIngredients(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllIngredients();
        }
        return ingredientRepository.searchByName(name);
    }
    @Override
    public List<Ingredient> getLowStockIngredients() {
        return ingredientRepository.findLowStockIngredients();
    }
    @Override
    public IngredientDTO convertToDTO(Ingredient ingredient) {
        IngredientDTO dto = new IngredientDTO();
        dto.setIngredientId(ingredient.getIngredientId());
        dto.setName(ingredient.getName());
        dto.setUnit(ingredient.getUnit());
        dto.setStockQuantity(ingredient.getStockQuantity());
        dto.setUnitPrice(ingredient.getUnitPrice());
        dto.setMinimumStock(ingredient.getMinimumStock());

        // Check if low stock
        if (ingredient.getMinimumStock() != null &&
                ingredient.getStockQuantity().compareTo(ingredient.getMinimumStock()) <= 0) {
            dto.setIsLowStock(true);
        } else {
            dto.setIsLowStock(false);
        }

        return dto;
    }
    private void validateIngredientDTO(IngredientDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Ingredient name is required");
        }
        if (dto.getUnit() == null || dto.getUnit().trim().isEmpty()) {
            throw new IllegalArgumentException("Unit is required");
        }
        if (dto.getStockQuantity() != null && dto.getStockQuantity().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        if (dto.getUnitPrice() != null && dto.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
    }
}