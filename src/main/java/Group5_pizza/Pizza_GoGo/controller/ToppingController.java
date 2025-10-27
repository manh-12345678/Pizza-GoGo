package Group5_pizza.Pizza_GoGo.controller;

import Group5_pizza.Pizza_GoGo.model.Topping;
import Group5_pizza.Pizza_GoGo.service.ToppingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/toppings")
public class ToppingController {

    private final ToppingService toppingService;

    public ToppingController(ToppingService toppingService) {
        this.toppingService = toppingService;
    }

    @GetMapping
    public String viewAll(@RequestParam(value = "name", required = false) String name, Model model) {
        try {
            List<Topping> toppings;
            if (name != null && !name.trim().isEmpty()) {
                toppings = toppingService.searchByName(name); // Cần thêm hàm này vào service
            } else {
                toppings = toppingService.getAll();
            }
            model.addAttribute("toppings", toppings != null ? toppings : Collections.emptyList());
            model.addAttribute("searchName", name);
        } catch (Exception e) {
            model.addAttribute("toppings", Collections.emptyList());
            model.addAttribute("error", "Đã xảy ra lỗi khi tải danh sách topping.");
        }
        return "toppings/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("topping", new Topping());
        return "toppings/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Topping topping = toppingService.getById(id);
        if (topping == null) {
            // Thêm thông báo lỗi và chuyển hướng nếu không tìm thấy
            return "redirect:/toppings";
        }
        model.addAttribute("topping", topping);
        return "toppings/form";
    }

    @PostMapping
    public String save(@ModelAttribute Topping topping, RedirectAttributes redirectAttributes) {
        try {
            if (topping.getToppingId() == null) {
                toppingService.create(topping);
                redirectAttributes.addFlashAttribute("success", "Đã thêm topping mới thành công!");
            } else {
                toppingService.update(topping.getToppingId(), topping);
                redirectAttributes.addFlashAttribute("success", "Đã cập nhật topping thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi lưu topping.");
        }
        return "redirect:/toppings";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            toppingService.softDelete(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa topping thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi xóa topping.");
        }
        return "redirect:/toppings";
    }

    @PostMapping("/{id}")
    public String saveUpdate(@PathVariable Integer id, @ModelAttribute Topping topping, RedirectAttributes redirectAttributes) {
        try {
            toppingService.update(id, topping);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật topping thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi cập nhật topping.");
        }
        return "redirect:/toppings";
    }
}