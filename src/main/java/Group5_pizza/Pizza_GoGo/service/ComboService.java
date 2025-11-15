package Group5_pizza.Pizza_GoGo.service;

import Group5_pizza.Pizza_GoGo.DTO.ComboDTO;
import Group5_pizza.Pizza_GoGo.model.Combo;

import java.util.List;

public interface ComboService {
    
    List<Combo> getAllCombos();
    
    List<Combo> getAllAvailableCombos();
    
    Combo getComboById(Integer comboId);
    
    Combo createCombo(ComboDTO comboDTO);
    
    Combo updateCombo(Integer comboId, ComboDTO comboDTO);
    
    boolean deleteCombo(Integer comboId);
    
    List<Combo> searchCombosByName(String name);
    
    List<Combo> getActiveCombos();
    
    ComboDTO convertToDTO(Combo combo);
    
    Combo convertToEntity(ComboDTO comboDTO);
}
