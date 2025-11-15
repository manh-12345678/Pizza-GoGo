package Group5_pizza.Pizza_GoGo.controller;

import Group5_pizza.Pizza_GoGo.DTO.ProductDTO;
import Group5_pizza.Pizza_GoGo.model.Category;
import Group5_pizza.Pizza_GoGo.model.Ingredient;
import Group5_pizza.Pizza_GoGo.model.Topping;
import Group5_pizza.Pizza_GoGo.service.CategoryService;
import Group5_pizza.Pizza_GoGo.service.IngredientService;
import Group5_pizza.Pizza_GoGo.service.ProductService;
import Group5_pizza.Pizza_GoGo.service.ToppingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller Quản lý Sản phẩm (ĐÃ ĐỒNG NHẤT)
 * Sử dụng /manage, /add, /edit
 * ĐÃ CẬP NHẬT: Thêm logic để quản lý Topping tùy chọn
 * THÊM API /api/products CHO ORDER MANAGEMENT
 */
@Controller
@RequestMapping({"/products","/manager/products"})
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final IngredientService ingredientService;
    private final ToppingService toppingService;

    // ==================== PHẦN QUẢN LÝ (GIỮ NGUYÊN 100%) ====================
    @GetMapping("/manage")
    public String listProducts(@RequestParam(required = false) String search,
                               @RequestParam(required = false) Integer categoryId,
                               Model model) {
        try {
            List<ProductDTO> products = productService.searchAndFilterProducts(search, categoryId);
            List<Category> categories = categoryService.getAllCategories();

            model.addAttribute("products", products);
            model.addAttribute("categories", categories);
            model.addAttribute("search", search);
            model.addAttribute("categoryId", categoryId);

            return "products/manage_products";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi tải danh sách sản phẩm: " + e.getMessage());
            return "products/manage_products";
        }
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        try {
            List<Category> categories = categoryService.getAllCategories();
            List<Ingredient> ingredients = ingredientService.getAllIngredients();
            List<Topping> toppings = toppingService.getAllToppings();

            model.addAttribute("productDTO", new ProductDTO());
            model.addAttribute("allCategories", categories);
            model.addAttribute("allIngredients", ingredients);
            model.addAttribute("allToppings", toppings);
            model.addAttribute("isEdit", false);

            return "products/product_form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi tải trang thêm mới: " + e.getMessage());
            return "redirect:/manager/products/manage";
        }
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute("productDTO") ProductDTO productDTO,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        try {
            productService.createProductWithDetails(productDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công!");
            return "redirect:/manager/products/manage";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi thêm sản phẩm: " + e.getMessage());
            model.addAttribute("productDTO", productDTO);
            model.addAttribute("allCategories", categoryService.getAllCategories());
            model.addAttribute("allIngredients", ingredientService.getAllIngredients());
            model.addAttribute("allToppings", toppingService.getAllToppings());
            model.addAttribute("isEdit", false);
            return "products/product_form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        if (id == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID sản phẩm không hợp lệ");
            return "redirect:/manager/products/manage";
        }
        try {
            ProductDTO productDTO = productService.getProductDTOById(id);
            List<Category> categories = categoryService.getAllCategories();
            List<Ingredient> ingredients = ingredientService.getAllIngredients();
            List<Topping> toppings = toppingService.getAllToppings();

            model.addAttribute("productDTO", productDTO);
            model.addAttribute("allCategories", categories);
            model.addAttribute("allIngredients", ingredients);
            model.addAttribute("allToppings", toppings);
            model.addAttribute("isEdit", true);

            return "products/product_form";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm: " + e.getMessage());
            return "redirect:/manager/products/manage";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable Integer id,
                                @ModelAttribute("productDTO") ProductDTO productDTO,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        try {
            productService.updateProductWithDetails(id, productDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
            return "redirect:/manager/products/manage";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
            model.addAttribute("productDTO", productDTO);
            model.addAttribute("allCategories", categoryService.getAllCategories());
            model.addAttribute("allIngredients", ingredientService.getAllIngredients());
            model.addAttribute("allToppings", toppingService.getAllToppings());
            model.addAttribute("isEdit", true);
            return "products/product_form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa sản phẩm: " + e.getMessage());
        }
        return "redirect:/manager/products/manage";
    }

    // ==================== THÊM API CHO ORDER MANAGEMENT (NHỎ GỌN) ====================
    @GetMapping("/api/products")
    @ResponseBody
    public List<ProductDTO> getAvailableProductsForOrder() {
        try {
            return productService.getAllProductsForOrder(); // Gọi service method mới
        } catch (Exception e) {
            return List.of(); // Trả về rỗng nếu lỗi
        }
    }
}