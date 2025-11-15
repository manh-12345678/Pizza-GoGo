// package Group5_pizza.Pizza_GoGo.repository;
// RestaurantTableRepository.java
package Group5_pizza.Pizza_GoGo.repository;
import Group5_pizza.Pizza_GoGo.model.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Integer> {
    long countByIsDeletedFalse();

    long countByStatusIgnoreCaseAndIsDeletedFalse(String status);
}