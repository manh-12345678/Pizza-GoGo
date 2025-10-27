package Group5_pizza.Pizza_GoGo.controller;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
import Group5_pizza.Pizza_GoGo.service.ProductToppingService;
import Group5_pizza.Pizza_GoGo.service.ToppingService;
import Group5_pizza.Pizza_GoGo.util.ProductValidator;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final ToppingService toppingService;
    private final ProductToppingService productToppingService;

    public ProductController(ProductService productService,
                             CategoryRepository categoryRepository,
                             ToppingService toppingService,
                             ProductToppingService productToppingService) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
        this.toppingService = toppingService;
        this.productToppingService = productToppingService;
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
        model.addAttribute("toppings", toppingService.getAll());
        model.addAttribute("selectedToppings", Collections.emptyList());
        return "products/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("toppings", toppingService.getAll());

        List<Integer> selectedToppings = productToppingService.getByProductId(id)
                .stream()
                .map(pt -> pt.getTopping().getToppingId())
                .collect(Collectors.toList());
        model.addAttribute("selectedToppings", selectedToppings);

        return "products/form";
    }

    @PostMapping
    public String createProduct(@ModelAttribute Product product,
                                @RequestParam(required = false, name = "toppingIds") List<Integer> toppingIds,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        // Validate dữ liệu
        List<String> errors = ProductValidator.validate(product);
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("toppings", toppingService.getAll());
            model.addAttribute("selectedToppings", toppingIds != null ? toppingIds : Collections.emptyList());
            return "products/form";
        }

        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product.setIsDeleted(false);

        Product savedProduct = productService.saveProduct(product);

        if (toppingIds != null) {
            productToppingService.saveProductToppings(savedProduct.getProductId(), toppingIds);
        }

        redirectAttributes.addFlashAttribute("success", "Đã thêm món " + product.getName() + " thành công!");
        return "redirect:/products";
    }

    @PostMapping("/{id}")
    public String updateProduct(@PathVariable Integer id,
                                @ModelAttribute Product product,
                                @RequestParam(required = false, name = "toppingIds") List<Integer> toppingIds,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        product.setProductId(id);

        List<String> errors = ProductValidator.validate(product);
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("toppings", toppingService.getAll());
            model.addAttribute("selectedToppings", toppingIds != null ? toppingIds : Collections.emptyList());
            return "products/form";
        }

        Product existing = productService.getProductById(id);
        if (existing == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm!");
            return "redirect:/products";
        }

        product.setCreatedAt(existing.getCreatedAt());
        product.setIsDeleted(existing.getIsDeleted());
        product.setUpdatedAt(LocalDateTime.now());

        productService.saveProduct(product);
        productToppingService.saveProductToppings(id, toppingIds);

        redirectAttributes.addFlashAttribute("success", "Món " + product.getName() + " đã được cập nhật!");
        return "redirect:/products";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Product product = productService.getProductById(id);
        if (product != null) {
            product.setIsDeleted(true);
            product.setUpdatedAt(LocalDateTime.now());
            productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("success", "Đã xóa món " + product.getName() + " thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy món để xóa!");
        }
        return "redirect:/products";
    }
}