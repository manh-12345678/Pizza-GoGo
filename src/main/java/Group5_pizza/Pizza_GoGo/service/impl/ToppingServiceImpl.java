// File: Group5_pizza.Pizza_GoGo.service.impl.ToppingServiceImpl.java
package Group5_pizza.Pizza_GoGo.service.impl;

import Group5_pizza.Pizza_GoGo.DTO.ToppingDTO;
import Group5_pizza.Pizza_GoGo.DTO.ToppingIngredientDTO;
import Group5_pizza.Pizza_GoGo.model.Ingredient;
import Group5_pizza.Pizza_GoGo.model.Topping;
import Group5_pizza.Pizza_GoGo.model.ToppingIngredient;
import Group5_pizza.Pizza_GoGo.repository.IngredientRepository;
import Group5_pizza.Pizza_GoGo.repository.ToppingIngredientRepository;
import Group5_pizza.Pizza_GoGo.repository.ToppingRepository;
import Group5_pizza.Pizza_GoGo.service.ToppingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ToppingServiceImpl implements ToppingService {

    private final ToppingRepository toppingRepository;
    private final IngredientRepository ingredientRepository;
    private final ToppingIngredientRepository toppingIngredientRepository; // Mặc dù không dùng, giữ lại để tương thích

    @Override
    @Transactional(readOnly = true)
    public List<ToppingDTO> searchToppings(String search) {
        List<Topping> toppings;
        boolean hasSearch = search != null && !search.trim().isEmpty();

        if (hasSearch) {
            toppings = toppingRepository.findByNameContainingAndIsDeletedFalse(search);
        } else {
            toppings = toppingRepository.findByIsDeletedFalse();
        }

        return toppings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ToppingDTO getToppingDTOById(Integer id) {
        Topping topping = getToppingById(id);
        return convertToDTO(topping);
    }

    @Override
    public Topping getToppingById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Topping ID không được để trống");
        }
        return toppingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Topping ID: " + id));
    }

    @Override
    @Transactional
    public Topping createToppingWithIngredients(ToppingDTO toppingDTO) {
        Topping topping = new Topping();
        topping.setIsDeleted(false);
        mapDtoToEntity(toppingDTO, topping); // Map Tên, Giá

        Topping savedTopping = toppingRepository.save(topping); // Lưu lần 1 để lấy ID

        Set<ToppingIngredient> toppingIngredients = mapIngredientsToEntity(toppingDTO, savedTopping);
        savedTopping.setToppingIngredients(toppingIngredients);

        return toppingRepository.save(savedTopping); // Lưu lần 2
    }

    @Override
    @Transactional
    public Topping updateToppingWithIngredients(Integer id, ToppingDTO toppingDTO) {
        Topping topping = getToppingById(id);
        topping.setUpdatedAt(LocalDateTime.now());
        mapDtoToEntity(toppingDTO, topping); // Cập nhật Tên, Giá

        // ❗ SỬA LỖI:
        // 1. Xóa NVL cũ
        // Chỉ cần gọi .clear()
        // JPA/Hibernate sẽ tự động xóa các bản ghi trong bảng trung gian
        // nhờ `orphanRemoval=true` trong Topping.java
        topping.getToppingIngredients().clear();

        // DÒNG NÀY BỊ XÓA:
        // toppingIngredientRepository.deleteAll(topping.getToppingIngredients());

        // 2. Thêm NVL mới
        Set<ToppingIngredient> toppingIngredients = mapIngredientsToEntity(toppingDTO, topping);
        topping.getToppingIngredients().addAll(toppingIngredients);

        return toppingRepository.save(topping);
    }

    @Override
    @Transactional
    public void deleteTopping(Integer id) {
        Topping topping = getToppingById(id);
        topping.setIsDeleted(true);
        topping.setUpdatedAt(LocalDateTime.now());
        toppingRepository.save(topping);
    }

    @Override
    public List<Topping> getAllToppings() {
        return toppingRepository.findByIsDeletedFalse();
    }

    // ========== HÀM HELPER (PRIVATE) ==========

    private ToppingDTO convertToDTO(Topping topping) {
        ToppingDTO dto = new ToppingDTO();
        dto.setToppingId(topping.getToppingId());
        dto.setName(topping.getName());
        dto.setPrice(topping.getPrice());

        List<ToppingIngredientDTO> ingredientDTOs = topping.getToppingIngredients().stream()
                .map(ti -> {
                    ToppingIngredientDTO ingDTO = new ToppingIngredientDTO();
                    ingDTO.setIngredientId(ti.getIngredient().getIngredientId());
                    ingDTO.setIngredientName(ti.getIngredient().getName());
                    ingDTO.setQuantityUsed(ti.getQuantityUsed());
                    ingDTO.setUnit(ti.getIngredient().getUnit());
                    return ingDTO;
                })
                .collect(Collectors.toList());

        dto.setIngredients(ingredientDTOs);
        return dto;
    }

    private void mapDtoToEntity(ToppingDTO toppingDTO, Topping topping) {
        topping.setName(toppingDTO.getName());
        topping.setPrice(toppingDTO.getPrice());
    }

    /**
     * ❗ FIX: Thêm check duplicate ingredient (tương tự Product)
     */
    private Set<ToppingIngredient> mapIngredientsToEntity(ToppingDTO toppingDTO, Topping topping) {
        Set<ToppingIngredient> toppingIngredients = new HashSet<>();
        Set<Integer> addedIngredientIds = new HashSet<>(); // ❗ FIX: Track duplicate

        if (toppingDTO.getIngredients() != null) {
            for (ToppingIngredientDTO ingDTO : toppingDTO.getIngredients()) {
                if (ingDTO.getIngredientId() != null &&
                        ingDTO.getQuantityUsed() != null &&
                        ingDTO.getQuantityUsed().compareTo(BigDecimal.ZERO) > 0 &&
                        !addedIngredientIds.contains(ingDTO.getIngredientId())) { // ❗ FIX: Check

                    Integer ingredientId = ingDTO.getIngredientId();
                    if (ingredientId == null) {
                        continue; // Skip null IDs
                    }
                    Ingredient ingredient = ingredientRepository.findById(ingredientId)
                            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Ingredient ID: " + ingredientId));

                    ToppingIngredient ti = new ToppingIngredient();
                    ti.setTopping(topping);
                    ti.setIngredient(ingredient);
                    ti.setQuantityUsed(ingDTO.getQuantityUsed());

                    toppingIngredients.add(ti);
                    addedIngredientIds.add(ingDTO.getIngredientId()); // ❗ FIX
                }
            }
        }
        return toppingIngredients;
    }

    @Override
    public Topping getToppingByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        return toppingRepository.findByNameAndIsDeletedFalse(name.trim())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy topping với tên: " + name));
    }
}