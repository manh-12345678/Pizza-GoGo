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

import Group5_pizza.Pizza_GoGo.model.Category;
import Group5_pizza.Pizza_GoGo.model.Product;
import Group5_pizza.Pizza_GoGo.repository.CategoryRepository;
import Group5_pizza.Pizza_GoGo.service.ProductService;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    public ProductController(ProductService productService, CategoryRepository categoryRepository) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public String getAllProducts(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            Model model) {

        List<Product> products = productService.searchProducts(name, categoryId);
        List<Category> categories = categoryRepository.findAll();

        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("searchName", name);
        model.addAttribute("searchCategoryId", categoryId);

        return "products/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());
        return "products/form";
    }

    // Thêm sản phẩm mới
    @PostMapping
    public String createProduct(@ModelAttribute Product product, RedirectAttributes redirectAttributes) {

        product.setIsDeleted(false);
        product.setCreatedAt(LocalDateTime.now());
        productService.saveProduct(product);
        redirectAttributes.addFlashAttribute("success", "Món " + product.getName() + " đã được tạo!");
        return "redirect:/products";
    }

    // Form chỉnh sửa sản phẩm
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepository.findAll());
        return "products/form";
    }

    // Cập nhật sản phẩm
    @PostMapping("/{id}")
    public String updateProduct(@PathVariable Integer id, @ModelAttribute Product product,
                                RedirectAttributes redirectAttributes) {

        product.setProductId(id);
        Product existing = productService.getProductById(id);
        product.setCreatedAt(existing.getCreatedAt());

        if (product.getIsDeleted() == null) {
            product.setIsDeleted(existing.getIsDeleted());
        }

        product.setUpdatedAt(LocalDateTime.now());

        productService.saveProduct(product);

        redirectAttributes.addFlashAttribute("success", "Món " + product.getName() + " đã được cập nhật!");
        return "redirect:/products";
    }

    // Xóa sản phẩm
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        productService.deleteProduct(id);
        redirectAttributes.addFlashAttribute("success", "Món đã bị xóa!");
        return "redirect:/products";
    }
}