// package Group5_pizza.Pizza_GoGo.service.impl;
// ProductToppingServiceImpl.java
package Group5_pizza.Pizza_GoGo.service.impl;
import Group5_pizza.Pizza_GoGo.model.Product;
import Group5_pizza.Pizza_GoGo.model.ProductTopping;
import Group5_pizza.Pizza_GoGo.model.Topping;
import Group5_pizza.Pizza_GoGo.repository.ProductRepository;
import Group5_pizza.Pizza_GoGo.repository.ProductToppingRepository;
import Group5_pizza.Pizza_GoGo.repository.ToppingRepository;
import Group5_pizza.Pizza_GoGo.service.ProductToppingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
@RequiredArgsConstructor
public class ProductToppingServiceImpl implements ProductToppingService {
    private final ProductToppingRepository productToppingRepository;
    private final ProductRepository productRepository;
    private final ToppingRepository toppingRepository;
    @Override
    public List<ProductTopping> getByProductId(Integer productId) {
        return productToppingRepository.findByProduct_ProductId(productId);
    }
    @Override
    @Transactional // Đảm bảo toàn vẹn dữ liệu: hoặc thành công tất cả, hoặc không thay đổi gì
    public void saveProductToppings(Integer productId, List<Integer> toppingIds) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID không được để trống");
        }
        if (toppingIds == null || toppingIds.isEmpty()) {
            // If no toppings provided, just delete existing ones
            productToppingRepository.deleteByProduct_ProductId(productId);
            return;
        }
        // Lấy thông tin sản phẩm và danh sách topping
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        List<Topping> toppings = toppingRepository.findAllById(toppingIds);
        // 1. Xóa tất cả các topping cũ của sản phẩm này
        productToppingRepository.deleteByProduct_ProductId(productId);
        // 2. Thêm lại các topping mới được chọn
        for (Topping topping : toppings) {
            ProductTopping productTopping = new ProductTopping();
            productTopping.setProduct(product);
            productTopping.setTopping(topping);
            productToppingRepository.save(productTopping);
        }
    }
    @Override
    @Transactional
    public void deleteByProductId(Integer productId) {
        productToppingRepository.deleteByProduct_ProductId(productId);
    }
}