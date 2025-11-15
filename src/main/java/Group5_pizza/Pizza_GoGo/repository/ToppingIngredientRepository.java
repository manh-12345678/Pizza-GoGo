// package Group5_pizza.Pizza_GoGo.repository;
// ToppingIngredientRepository.java
package Group5_pizza.Pizza_GoGo.repository;
import Group5_pizza.Pizza_GoGo.model.ToppingIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ToppingIngredientRepository extends JpaRepository<ToppingIngredient, Integer> {
}