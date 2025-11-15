package Group5_pizza.Pizza_GoGo.repository;

import Group5_pizza.Pizza_GoGo.model.Combo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComboRepository extends JpaRepository<Combo, Integer> {
    
    @EntityGraph(attributePaths = {"comboDetails", "comboDetails.product"})
    List<Combo> findByIsDeletedFalseOrIsDeletedNull();
    
    @EntityGraph(attributePaths = {"comboDetails", "comboDetails.product"})
    Optional<Combo> findByComboIdAndIsDeletedFalse(Integer comboId);
    
    List<Combo> findByNameContainingIgnoreCaseAndIsDeletedFalse(String name);
    
    List<Combo> findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndIsDeletedFalse(
        LocalDateTime startDate, LocalDateTime endDate);
    
    @EntityGraph(attributePaths = {"comboDetails", "comboDetails.product"})
    @Override
    @NonNull
    Optional<Combo> findById(@NonNull Integer comboId);
}
