package Group5_pizza.Pizza_GoGo.repository;

import Group5_pizza.Pizza_GoGo.model.ProductTopping;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductToppingRepository extends JpaRepository<ProductTopping, Integer> {
    List<ProductTopping> findByProduct_ProductId(Integer productId);
    void deleteByProduct_ProductId(Integer productId);
}