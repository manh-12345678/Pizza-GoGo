// package Group5_pizza.Pizza_GoGo.service.impl;
// ProductServiceImpl.java
package Group5_pizza.Pizza_GoGo.service.impl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList; // ❗ Import *
import java.util.HashSet; // ❗ Import *
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import Group5_pizza.Pizza_GoGo.DTO.ProductDTO;
import Group5_pizza.Pizza_GoGo.DTO.ProductIngredientDTO;
import Group5_pizza.Pizza_GoGo.model.Category;
import Group5_pizza.Pizza_GoGo.model.Ingredient;
import Group5_pizza.Pizza_GoGo.model.Product;
import Group5_pizza.Pizza_GoGo.model.ProductIngredient;
import Group5_pizza.Pizza_GoGo.model.ProductTopping;
import Group5_pizza.Pizza_GoGo.model.Topping;
import Group5_pizza.Pizza_GoGo.repository.CategoryRepository;
import Group5_pizza.Pizza_GoGo.repository.IngredientRepository;
import Group5_pizza.Pizza_GoGo.repository.ProductIngredientRepository;
import Group5_pizza.Pizza_GoGo.repository.ProductRepository;
import Group5_pizza.Pizza_GoGo.repository.ProductToppingRepository;
import Group5_pizza.Pizza_GoGo.repository.ToppingRepository;
import Group5_pizza.Pizza_GoGo.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductIngredientRepository productIngredientRepository;
    // ❗ ================= THÊM REPOSITORY =================
    private final ToppingRepository toppingRepository;
    private final ProductToppingRepository productToppingRepository;
    // ❗ ===================================================
    // DÀNH CHO ORDER MANAGEMENT
    @Override
    public List<ProductDTO> getAllProductsForOrder() {
        return productRepository.findByIsDeletedFalse().stream()
                .map(this::toProductDTOForOrder)
                .toList();
    }
    private ProductDTO toProductDTOForOrder(Product p) {
        ProductDTO dto = new ProductDTO();
        dto.setProductId(p.getProductId());
        dto.setName(p.getName());
        dto.setPrice(p.getPrice());
        dto.setToppings(p.getToppings().stream()
                .filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()))
                .map(t -> new ProductDTO.ToppingDTO(t.getToppingId(), t.getName(), t.getPrice()))
                .toList());
        return dto;
    }
    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> searchAndFilterProducts(String search, Integer categoryId) {
        List<Product> products;
        boolean hasSearch = search != null && !search.trim().isEmpty();
        boolean hasCategory = categoryId != null;
        if (hasSearch && hasCategory) {
            products = productRepository.findByNameContainingAndCategoryCategoryIdAndIsDeletedFalse(search, categoryId);
        } else if (hasSearch) {
            products = productRepository.findByNameContainingAndIsDeletedFalse(search);
        } else if (hasCategory) {
            products = productRepository.findByCategoryCategoryIdAndIsDeletedFalse(categoryId);
        } else {
            products = productRepository.findByIsDeletedFalse();
        }
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductDTOById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Product ID không được để trống");
        }
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
        return convertToDTO(product);
    }
    @Override
    public Product getProductById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Product ID không được để trống");
        }
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
    }
    
    @Override
    public Product getProductByIdWithToppings(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Product ID không được để trống");
        }
        return productRepository.findByIdWithToppings(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
    }
    @Override
    public List<Product> getAllProducts() {
        return productRepository.findByIsDeletedFalse();
    }
    @Override
    @Transactional
    public Product createProductWithDetails(ProductDTO productDTO) { // ❗ ĐỔI TÊN HÀM
        Product product = new Product();
        product.setIsDeleted(false);
        // 1. Map thông tin cơ bản (Tên, Giá, Category...)
        mapDtoToEntity(productDTO, product);
        Product savedProduct = productRepository.save(product); // Lưu lần 1 để lấy ID
        // 2. Map danh sách nguyên vật liệu (ProductIngredient)
        Set<ProductIngredient> productIngredients = mapIngredientsToEntity(productDTO, savedProduct);
        savedProduct.setProductIngredients(productIngredients);
        // 3. ❗ Map danh sách Topping tùy chọn (ProductTopping)
        mapToppingsToEntity(productDTO, savedProduct); // ❗ GỌI HÀM MỚI
        return productRepository.save(savedProduct); // Lưu lần 2 với đầy đủ thông tin
    }
    @Override
    @Transactional
    public Product updateProductWithDetails(Integer id, ProductDTO productDTO) { // ❗ ĐỔI TÊN HÀM
        Product product = getProductById(id);
        product.setUpdatedAt(LocalDateTime.now());
        // 1. Cập nhật thông tin cơ bản
        mapDtoToEntity(productDTO, product);
        // 2. Cập nhật NVL (ProductIngredient)
        product.getProductIngredients().clear(); // Xóa cũ (nhờ orphanRemoval=true)
        Set<ProductIngredient> productIngredients = mapIngredientsToEntity(productDTO, product);
        product.getProductIngredients().addAll(productIngredients); // Thêm mới
        // 3. ❗ Cập nhật Topping tùy chọn (ProductTopping)
        mapToppingsToEntity(productDTO, product); // ❗ GỌI HÀM MỚI
        return productRepository.save(product);
    }
    @Override
    @Transactional
    public void deleteProduct(Integer id) {
        Product product = getProductById(id);
        product.setIsDeleted(true);
        product.setUpdatedAt(LocalDateTime.now());
        // ❗ Xóa các liên kết topping khi xóa sản phẩm
        productToppingRepository.deleteByProduct(product);
        productRepository.save(product);
    }
    // ========== HÀM HELPER (PRIVATE) ==========
    /**
     * Chuyển từ Entity Product sang ProductDTO (cho việc hiển thị)
     */
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setDescription(product.getDescription());
        dto.setImgUrl(product.getImgUrl());
        dto.setQuantityPerDay(product.getQuantityPerDay());
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getCategoryId());
            dto.setCategoryName(product.getCategory().getCategoryName());
        }
        // Chuyển danh sách NVL (ProductIngredient)
        List<ProductIngredientDTO> ingredientDTOs = product.getProductIngredients().stream()
                .map(pi -> {
                    ProductIngredientDTO ingDTO = new ProductIngredientDTO();
                    ingDTO.setIngredientId(pi.getIngredient().getIngredientId());
                    ingDTO.setIngredientName(pi.getIngredient().getName());
                    ingDTO.setQuantityUsed(pi.getQuantityUsed());
                    ingDTO.setUnit(pi.getIngredient().getUnit());
                    return ingDTO;
                })
                .collect(Collectors.toList());
        dto.setIngredients(ingredientDTOs);
        // ❗ THÊM: Chuyển danh sách Topping (ProductTopping) sang danh sách ID
        List<Integer> toppingIds = product.getProductToppings().stream()
                .map(productTopping -> productTopping.getTopping().getToppingId())
                .collect(Collectors.toList());
        dto.setToppingIds(toppingIds);
        return dto;
    }
    /**
     * Map thông tin cơ bản từ DTO sang Entity (cho việc lưu/cập nhật)
     */
    private void mapDtoToEntity(ProductDTO productDTO, Product product) {
        product.setName(productDTO.getName());
        product.setPrice(productDTO.getPrice());
        product.setDescription(productDTO.getDescription());
        product.setImgUrl(productDTO.getImgUrl());
        product.setQuantityPerDay(productDTO.getQuantityPerDay());
        Integer categoryId = productDTO.getCategoryId();
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID không được để trống");
        }
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Category ID: " + categoryId));
        product.setCategory(category);
    }
    /**
     * Map danh sách NVL từ DTO sang Set<ProductIngredient> (cho việc lưu/cập
     * nhật) ❗ FIX: Thêm check duplicate ingredient trước khi add (tránh 2 rows
     * same ingredient)
     */
    private Set<ProductIngredient> mapIngredientsToEntity(ProductDTO productDTO, Product product) {
        Set<ProductIngredient> productIngredients = new HashSet<>();
        Set<Integer> addedIngredientIds = new HashSet<>(); // ❗ FIX: Track để tránh duplicate
        if (productDTO.getIngredients() != null) {
            for (ProductIngredientDTO ingDTO : productDTO.getIngredients()) {
                if (ingDTO.getIngredientId() != null
                        && ingDTO.getQuantityUsed() != null
                        && ingDTO.getQuantityUsed().compareTo(BigDecimal.ZERO) > 0
                        && !addedIngredientIds.contains(ingDTO.getIngredientId())) { // ❗ FIX: Check duplicate
                    Integer ingredientId = ingDTO.getIngredientId();
                    if (ingredientId == null) {
                        continue; // Skip null IDs
                    }
                    Ingredient ingredient = ingredientRepository.findById(ingredientId)
                            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Ingredient ID: " + ingredientId));
                    ProductIngredient pi = new ProductIngredient();
                    pi.setProduct(product);
                    pi.setIngredient(ingredient);
                    pi.setQuantityUsed(ingDTO.getQuantityUsed());
                    productIngredients.add(pi);
                    addedIngredientIds.add(ingDTO.getIngredientId()); // ❗ FIX: Add to set
                }
            }
        }
        return productIngredients;
    }
    /**
     * ❗ HÀM MỚI: Map danh sách Topping IDs từ DTO sang bảng ProductTopping
     */
    @Transactional
    private void mapToppingsToEntity(ProductDTO productDTO, Product product) {
        // 1. Xóa tất cả các liên kết ProductTopping CŨ của sản phẩm này
        productToppingRepository.deleteByProduct(product);
        // 2. Tạo lại các liên kết MỚI
        if (productDTO.getToppingIds() != null && !productDTO.getToppingIds().isEmpty()) {
            List<ProductTopping> newProductToppings = new ArrayList<>();
            for (Integer toppingId : productDTO.getToppingIds()) {
                if (toppingId == null) {
                    continue; // Skip null IDs
                }
                Topping topping = toppingRepository.findById(toppingId)
                        .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Topping ID: " + toppingId));
                ProductTopping productTopping = new ProductTopping();
                productTopping.setProduct(product);
                productTopping.setTopping(topping);
                newProductToppings.add(productTopping);
            }
            // 3. Lưu tất cả liên kết mới
            productToppingRepository.saveAll(newProductToppings);
        }
        // Nếu productDTO.getToppingIds() là null hoặc rỗng, thì không làm gì,
        // đồng nghĩa với việc tất cả liên kết đã bị xóa ở bước 1.
    }
}