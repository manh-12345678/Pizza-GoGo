package Group5_pizza.Pizza_GoGo.repository;

import Group5_pizza.Pizza_GoGo.model.Topping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToppingRepository extends JpaRepository<Topping, Integer> {
    List<Topping> findByIsDeletedFalse();

    List<Topping> findByNameContainingIgnoreCaseAndIsDeletedFalse(String name);
}