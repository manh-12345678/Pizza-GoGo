package Group5_pizza.Pizza_GoGo.repository;

import Group5_pizza.Pizza_GoGo.model.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByCategory_CategoryId(Integer categoryId);

    List<Product> findByNameContainingIgnoreCase(String name);


    @EntityGraph(attributePaths = {"category", "productToppings", "productToppings.topping"})
    List<Product> findByIsDeletedFalseOrIsDeletedNull();

    List<Product> findByNameContainingIgnoreCaseAndCategory_CategoryId(String name, Integer categoryId);
}