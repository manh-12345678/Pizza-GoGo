// package Group5_pizza.Pizza_GoGo.repository;
// ProductIngredientRepository.java
package Group5_pizza.Pizza_GoGo.repository;
import Group5_pizza.Pizza_GoGo.model.ProductIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ProductIngredientRepository extends JpaRepository<ProductIngredient, Integer> {
}