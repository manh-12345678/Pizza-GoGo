package Group5_pizza.Pizza_GoGo.repository;

import Group5_pizza.Pizza_GoGo.model.Product;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByCategory_CategoryId(Integer categoryId);

    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByIsDeletedFalse();

    List<Product> findByNameContainingIgnoreCaseAndCategory_CategoryId(String name, Integer categoryId);
}