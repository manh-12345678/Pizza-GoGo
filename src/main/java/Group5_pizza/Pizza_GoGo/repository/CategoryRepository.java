package Group5_pizza.Pizza_GoGo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import Group5_pizza.Pizza_GoGo.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findByCategoryNameContainingIgnoreCase(String keyword);
}
