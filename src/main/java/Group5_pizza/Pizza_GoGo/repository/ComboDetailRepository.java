package Group5_pizza.Pizza_GoGo.repository;

import Group5_pizza.Pizza_GoGo.model.ComboDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComboDetailRepository extends JpaRepository<ComboDetail, Integer> {
    
    List<ComboDetail> findByCombo_ComboId(Integer comboId);
    
    void deleteByCombo_ComboId(Integer comboId);
}
