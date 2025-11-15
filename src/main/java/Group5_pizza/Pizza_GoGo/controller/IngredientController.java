package Group5_pizza.Pizza_GoGo.controller;

import Group5_pizza.Pizza_GoGo.DTO.IngredientDTO;
import Group5_pizza.Pizza_GoGo.model.Ingredient;
import Group5_pizza.Pizza_GoGo.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for Ingredient Management with full CRUD operations
 */
@Controller
@RequestMapping({"/ingredients","/manager/ingredients"})
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    // ========== WEB VIEWS (HTML) ==========

    /**
     * Display list of all ingredients
     * ❗ ĐÃ CẬP NHẬT: Đổi đường dẫn sang /manage để đồng nhất
     */
    @GetMapping("/manage") // ❗ Cập nhật
    public String listIngredients(@RequestParam(required = false) String search, Model model) {
        try {
            model.addAttribute("activePage", "ingredients");
            
            List<Ingredient> ingredients;
            if (search != null && !search.trim().isEmpty()) {
                ingredients = ingredientService.searchIngredients(search);
                model.addAttribute("search", search);
            } else {
                ingredients = ingredientService.getAllIngredients();
            }

            List<IngredientDTO> ingredientDTOs = ingredients.stream()
                    .map(ingredientService::convertToDTO)
                    .toList();

            model.addAttribute("ingredients", ingredientDTOs);

            // Check for low stock items
            List<Ingredient> lowStock = ingredientService.getLowStockIngredients();
            if (!lowStock.isEmpty()) {
                model.addAttribute("lowStockWarning", lowStock.size() + " ingredient(s) are low on stock!");
            }

            return "ingredients/ingredient-list";
        } catch (Exception e) {
            model.addAttribute("activePage", "ingredients");
            model.addAttribute("errorMessage", "Error loading ingredients: " + e.getMessage());
            return "ingredients/ingredient-list";
        }
    }

    /**
     * Show form to add new ingredient
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("activePage", "ingredients");
        model.addAttribute("ingredientDTO", new IngredientDTO());
        model.addAttribute("isEdit", false);
        return "ingredients/ingredient-form";
    }

    /**
     * Process adding new ingredient
     */
    @PostMapping("/add")
    public String addIngredient(@ModelAttribute("ingredientDTO") IngredientDTO ingredientDTO,
                                RedirectAttributes redirectAttributes,
                                Model model) { // ❗ Phải có Model
        try {
            ingredientService.createIngredient(ingredientDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm nguyên vật liệu thành công!");
            return "redirect:/manager/ingredients/manage"; // ❗ Cập nhật
        } catch (IllegalArgumentException e) {
            model.addAttribute("activePage", "ingredients");
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("ingredientDTO", ingredientDTO);
            model.addAttribute("isEdit", false); // ❗ SỬA LỖI: Thêm dòng này
            return "ingredients/ingredient-form";
        } catch (Exception e) {
            model.addAttribute("activePage", "ingredients");
            model.addAttribute("errorMessage", "Lỗi khi thêm nguyên vật liệu: " + e.getMessage());
            model.addAttribute("ingredientDTO", ingredientDTO);
            model.addAttribute("isEdit", false); // ❗ SỬA LỖI: Thêm dòng này
            return "ingredients/ingredient-form";
        }
    }

    /**
     * Show form to edit ingredient
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) { // ❗ Thêm RedirectAttributes
        try {
            model.addAttribute("activePage", "ingredients");
            Ingredient ingredient = ingredientService.getIngredientById(id);
            IngredientDTO ingredientDTO = ingredientService.convertToDTO(ingredient);

            model.addAttribute("ingredientDTO", ingredientDTO);
            model.addAttribute("isEdit", true);
            return "ingredients/ingredient-form";
        } catch (RuntimeException e) {
            // ❗ Cập nhật: Dùng RedirectAttributes để thông báo lỗi
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy nguyên vật liệu");
            return "redirect:/manager/ingredients/manage"; // ❗ Cập nhật
        }
    }

    /**
     * Process updating ingredient
     */
    @PostMapping("/edit/{id}")
    public String updateIngredient(@PathVariable Integer id,
                                   @ModelAttribute("ingredientDTO") IngredientDTO ingredientDTO,
                                   RedirectAttributes redirectAttributes,
                                   Model model) { // ❗ Phải có Model
        try {
            ingredientService.updateIngredient(id, ingredientDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật nguyên vật liệu thành công!");
            return "redirect:/manager/ingredients/manage"; // ❗ Cập nhật
        } catch (IllegalArgumentException e) {
            model.addAttribute("activePage", "ingredients");
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("ingredientDTO", ingredientDTO);
            model.addAttribute("isEdit", true); // ❗ SỬA LỖI: Thêm dòng này
            return "ingredients/ingredient-form";
        } catch (Exception e) {
            model.addAttribute("activePage", "ingredients");
            model.addAttribute("errorMessage", "Lỗi khi cập nhật nguyên vật liệu: " + e.getMessage());
            model.addAttribute("ingredientDTO", ingredientDTO);
            model.addAttribute("isEdit", true); // ❗ SỬA LỖI: Thêm dòng này
            return "ingredients/ingredient-form";
        }
    }

    /**
     * Delete ingredient (soft delete)
     */
    @PostMapping("/delete/{id}")
    public String deleteIngredient(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            ingredientService.deleteIngredient(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa nguyên vật liệu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa nguyên vật liệu: " + e.getMessage());
        }
        return "redirect:/manager/ingredients/manage"; // ❗ Cập nhật
    }
    // ========== REST API ENDPOINTS ==========

    /**
     * API: Get all ingredients
     */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<List<IngredientDTO>> getAllIngredientsApi() {
        try {
            List<Ingredient> ingredients = ingredientService.getAllIngredients();
            List<IngredientDTO> ingredientDTOs = ingredients.stream()
                    .map(ingredientService::convertToDTO)
                    .toList();
            return ResponseEntity.ok(ingredientDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * API: Get ingredient by ID
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<IngredientDTO> getIngredientByIdApi(@PathVariable Integer id) {
        try {
            Ingredient ingredient = ingredientService.getIngredientById(id);
            IngredientDTO ingredientDTO = ingredientService.convertToDTO(ingredient);
            return ResponseEntity.ok(ingredientDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * API: Create ingredient
     */
    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createIngredientApi(@RequestBody IngredientDTO ingredientDTO) {
        Map<String, Object> response = new HashMap<>();
        try {
            Ingredient ingredient = ingredientService.createIngredient(ingredientDTO);
            IngredientDTO resultDTO = ingredientService.convertToDTO(ingredient);
            
            response.put("success", true);
            response.put("message", "Ingredient created successfully");
            response.put("data", resultDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating ingredient: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API: Update ingredient
     */
    @PutMapping("/api/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateIngredientApi(@PathVariable Integer id,
                                                                   @RequestBody IngredientDTO ingredientDTO) {
        Map<String, Object> response = new HashMap<>();
        try {
            Ingredient ingredient = ingredientService.updateIngredient(id, ingredientDTO);
            IngredientDTO resultDTO = ingredientService.convertToDTO(ingredient);
            
            response.put("success", true);
            response.put("message", "Ingredient updated successfully");
            response.put("data", resultDTO);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", "Ingredient not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating ingredient: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API: Delete ingredient
     */
    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteIngredientApi(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            ingredientService.deleteIngredient(id);
            response.put("success", true);
            response.put("message", "Ingredient deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", "Ingredient not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting ingredient: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API: Get low stock ingredients
     */
    @GetMapping("/api/low-stock")
    @ResponseBody
    public ResponseEntity<List<IngredientDTO>> getLowStockIngredientsApi() {
        try {
            List<Ingredient> lowStock = ingredientService.getLowStockIngredients();
            List<IngredientDTO> ingredientDTOs = lowStock.stream()
                    .map(ingredientService::convertToDTO)
                    .toList();
            return ResponseEntity.ok(ingredientDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
