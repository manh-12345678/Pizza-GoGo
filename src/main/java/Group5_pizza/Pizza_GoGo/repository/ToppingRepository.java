package Group5_pizza.Pizza_GoGo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import Group5_pizza.Pizza_GoGo.model.Topping;

public interface ToppingRepository extends JpaRepository<Topping, Integer> {
    List<Topping> findByIsDeletedFalse();
    List<Topping> findByNameContainingIgnoreCase(String name);
}
