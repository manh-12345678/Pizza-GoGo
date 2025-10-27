package Group5_pizza.Pizza_GoGo.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import Group5_pizza.Pizza_GoGo.model.Topping;
import Group5_pizza.Pizza_GoGo.service.ToppingService;

@Controller
@RequestMapping("/toppings")
public class ToppingController {

    private final ToppingService toppingService;

    public ToppingController(ToppingService toppingService) {
        this.toppingService = toppingService;
    }

    @GetMapping
    public String getAllToppings(@RequestParam(value = "name", required = false) String name, Model model) {
        List<Topping> toppings = toppingService.searchToppings(name);
        model.addAttribute("toppings", toppings);
        model.addAttribute("searchName", name);
        return "toppings/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("topping", new Topping());
        return "toppings/form";
    }

    @PostMapping
    public String createTopping(@ModelAttribute Topping topping, RedirectAttributes redirectAttributes) {
        topping.setIsDeleted(false);
        topping.setCreatedAt(LocalDateTime.now());
        toppingService.saveTopping(topping);
        redirectAttributes.addFlashAttribute("success", "Topping " + topping.getName() + " đã được tạo!");
        return "redirect:/toppings";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Topping topping = toppingService.getToppingById(id);
        model.addAttribute("topping", topping);
        return "toppings/form"; 
    }

    @PostMapping("/{id}")
    public String updateTopping(@PathVariable Integer id, @ModelAttribute Topping topping,
                                RedirectAttributes redirectAttributes) {
        Topping existing = toppingService.getToppingById(id);
    
        topping.setToppingId(id);
        topping.setCreatedAt(existing.getCreatedAt());
        topping.setUpdatedAt(LocalDateTime.now());

        if (topping.getIsDeleted() == null) {
            topping.setIsDeleted(existing.getIsDeleted());
        }

        toppingService.saveTopping(topping);
        redirectAttributes.addFlashAttribute("success", "Topping " + topping.getName() + " đã được cập nhật!");
        return "redirect:/toppings";
    }

    @GetMapping("/delete/{id}")
    public String deleteTopping(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        toppingService.deleteTopping(id);
        redirectAttributes.addFlashAttribute("success", "Topping đã bị xóa!");
        return "redirect:/toppings";
    }
}
