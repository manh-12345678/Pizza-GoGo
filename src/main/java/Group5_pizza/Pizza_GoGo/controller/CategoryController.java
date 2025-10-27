package Group5_pizza.Pizza_GoGo.controller;

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

import Group5_pizza.Pizza_GoGo.model.Category;
import Group5_pizza.Pizza_GoGo.service.CategoryService;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listCategories(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<Category> categories = (keyword != null && !keyword.isEmpty())
                ? categoryService.searchCategories(keyword)
                : categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);
        return "categories/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new Category());
        return "categories/form";
    }

    @PostMapping
    public String createCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        categoryService.saveCategory(category);
        redirectAttributes.addFlashAttribute("success", "Category '" + category.getCategoryName() + "' đã được tạo!");
        return "redirect:/categories";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Category category = categoryService.getCategoryById(id);
        model.addAttribute("category", category);
        return "categories/form";
    }

    @PostMapping("/{id}")
    public String updateCategory(@PathVariable Integer id, @ModelAttribute Category category,
     RedirectAttributes redirectAttributes) {

        category.setCategoryId(id);
        categoryService.saveCategory(category);
        redirectAttributes.addFlashAttribute("success",
                "Category '" + category.getCategoryName() + "' đã được cập nhật!");
        return "redirect:/categories";
    }

@GetMapping("/delete/{id}")
public String deleteCategory(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
    boolean deleted = categoryService.deleteCategory(id);

    if (deleted) {
        redirectAttributes.addFlashAttribute("success", "Category đã bị xóa!");
    } else {
        redirectAttributes.addFlashAttribute("error", "Không thể xóa! Category này vẫn còn sản phẩm.");
    }

    return "redirect:/categories";
}
}

