// package Group5_pizza.Pizza_GoGo.repository;
// IngredientRepository.java
package Group5_pizza.Pizza_GoGo.repository;
import Group5_pizza.Pizza_GoGo.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Integer> {

    // Find all non-deleted ingredients
    List<Ingredient> findByIsDeletedFalse();

    // Find by ID and not deleted
    Optional<Ingredient> findByIngredientIdAndIsDeletedFalse(Integer id);

    // Search by name (case-insensitive)
    @Query("SELECT i FROM Ingredient i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%')) AND i.isDeleted = false")
    List<Ingredient> searchByName(String name);

    // Find low stock ingredients
    @Query("SELECT i FROM Ingredient i WHERE i.stockQuantity <= i.minimumStock AND i.isDeleted = false")
    List<Ingredient> findLowStockIngredients();
}