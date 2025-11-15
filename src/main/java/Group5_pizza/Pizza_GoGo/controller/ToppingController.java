// File: Group5_pizza.Pizza_GoGo.controller.ToppingController.java
package Group5_pizza.Pizza_GoGo.controller;

import Group5_pizza.Pizza_GoGo.DTO.ToppingDTO;
import Group5_pizza.Pizza_GoGo.model.Ingredient;
import Group5_pizza.Pizza_GoGo.service.IngredientService;
import Group5_pizza.Pizza_GoGo.service.ToppingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller Quản lý Topping (ĐÃ ĐỒNG NHẤT)
 * Sử dụng /manage, /add, /edit
 */
@Controller
@RequestMapping({"/toppings","/manager/toppings"})
@RequiredArgsConstructor
public class ToppingController {

    private final ToppingService toppingService;
    private final IngredientService ingredientService;

    /**
     * Hiển thị trang quản lý (đồng nhất với Combo)
     */
    @GetMapping("/manage")
    public String listToppings(@RequestParam(required = false) String search, Model model) {
        try {
            List<ToppingDTO> toppings = toppingService.searchToppings(search);
            model.addAttribute("toppings", toppings);
            model.addAttribute("search", search);
            return "toppings/manage_toppings"; // ❗ Trả về file mới
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi tải danh sách topping: " + e.getMessage());
            return "toppings/manage_toppings";
        }
    }

    /**
     * Hiển thị form Thêm mới
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        try {
            List<Ingredient> ingredients = ingredientService.getAllIngredients();
            model.addAttribute("toppingDTO", new ToppingDTO());
            model.addAttribute("allIngredients", ingredients);
            model.addAttribute("isEdit", false);
            return "toppings/topping_form"; // ❗ Trả về file mới
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi tải trang thêm mới: " + e.getMessage());
            return "redirect:/manager/toppings/manage";
        }
    }

    /**
     * Xử lý Thêm mới
     */
    @PostMapping("/add")
    public String addTopping(@ModelAttribute("toppingDTO") ToppingDTO toppingDTO,
                             RedirectAttributes redirectAttributes, Model model) {
        try {
            toppingService.createToppingWithIngredients(toppingDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm topping thành công!");
            return "redirect:/manager/toppings/manage";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi thêm topping: " + e.getMessage());
            model.addAttribute("toppingDTO", toppingDTO);
            model.addAttribute("allIngredients", ingredientService.getAllIngredients());
            model.addAttribute("isEdit", false);
            return "toppings/topping_form"; // ❗ Trả về file mới
        }
    }

    /**
     * Hiển thị form Chỉnh sửa
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        if (id == null) { // ❗ FIX: Tránh TypeMismatchException
            redirectAttributes.addFlashAttribute("errorMessage", "ID topping không hợp lệ");
            return "redirect:/manager/toppings/manage";
        }
        try {
            ToppingDTO toppingDTO = toppingService.getToppingDTOById(id);
            List<Ingredient> ingredients = ingredientService.getAllIngredients();

            model.addAttribute("toppingDTO", toppingDTO);
            model.addAttribute("allIngredients", ingredients);
            model.addAttribute("isEdit", true);
            return "toppings/topping_form"; // ❗ Trả về file mới
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy topping: " + e.getMessage());
            return "redirect:/manager/toppings/manage";
        }
    }

    /**
     * Xử lý Chỉnh sửa
     */
    @PostMapping("/edit/{id}")
    public String updateTopping(@PathVariable Integer id,
                                @ModelAttribute("toppingDTO") ToppingDTO toppingDTO,
                                RedirectAttributes redirectAttributes, Model model) {
        try {
            toppingService.updateToppingWithIngredients(id, toppingDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật topping thành công!");
            return "redirect:/manager/toppings/manage";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi cập nhật topping: " + e.getMessage());
            model.addAttribute("toppingDTO", toppingDTO);
            model.addAttribute("allIngredients", ingredientService.getAllIngredients());
            model.addAttribute("isEdit", true);
            return "toppings/topping_form"; // ❗ Trả về file mới
        }
    }

    /**
     * Xử lý Xóa (mềm)
     */
    @PostMapping("/delete/{id}")
    public String deleteTopping(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            toppingService.deleteTopping(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa topping thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa topping: " + e.getMessage());
        }
        return "redirect:/manager/toppings/manage";
    }
}