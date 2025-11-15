// src/main/java/Group5_pizza/Pizza_GoGo/repository/ToppingRepository.java
package Group5_pizza.Pizza_GoGo.repository;

import Group5_pizza.Pizza_GoGo.model.Topping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ToppingRepository extends JpaRepository<Topping, Integer> {

    List<Topping> findByIsDeletedFalse();

    List<Topping> findByNameContainingAndIsDeletedFalse(String name);

    // THÊM MỚI: Tìm chính xác theo tên + chưa xóa
    Optional<Topping> findByNameAndIsDeletedFalse(String name);
}