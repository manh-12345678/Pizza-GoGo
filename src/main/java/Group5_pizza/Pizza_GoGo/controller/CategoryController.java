package Group5_pizza.Pizza_GoGo.controller;

import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import Group5_pizza.Pizza_GoGo.model.Category;
import Group5_pizza.Pizza_GoGo.repository.CategoryRepository;

@Controller
@RequestMapping({"/categories","/manager/categories"})
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public String listCategories(Model model) {
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        return "categories/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new Category());
        return "categories/form";
    }

    @PostMapping
    public String createCategory(@ModelAttribute @NonNull Category category, RedirectAttributes redirectAttributes) {
        categoryRepository.save(category);
        redirectAttributes.addFlashAttribute("success", "Category '" + category.getCategoryName() + "' đã được tạo!");
        return "redirect:/manager/categories";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable @NonNull Integer id, Model model) {
        Category category = categoryRepository.findById(id).orElse(null);
        model.addAttribute("category", category);
        return "categories/form";
    }

    @PostMapping("/{id}")
    public String updateCategory(@PathVariable @NonNull Integer id, @ModelAttribute @NonNull Category category,
            RedirectAttributes redirectAttributes) {
        category.setCategoryId(id);
        categoryRepository.save(category);
        redirectAttributes.addFlashAttribute("success",
                "Category '" + category.getCategoryName() + "' đã được cập nhật!");
        return "redirect:/manager/categories";
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable @NonNull Integer id, RedirectAttributes redirectAttributes) {
        categoryRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Category đã bị xóa!");
        return "redirect:/manager/categories";
    }
}
