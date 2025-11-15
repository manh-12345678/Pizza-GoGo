// File: Group5_pizza.Pizza_GoGo.service.impl.InventoryServiceImpl.java
package Group5_pizza.Pizza_GoGo.service.impl;

import Group5_pizza.Pizza_GoGo.model.*;
import Group5_pizza.Pizza_GoGo.repository.IngredientRepository;
import Group5_pizza.Pizza_GoGo.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final IngredientRepository ingredientRepository;

    /**
     * Trừ kho cho một đơn hàng.
     * Hàm này yêu cầu một transaction đang chạy (Propagation.REQUIRED)
     * và sẽ rollback nếu có lỗi (ví dụ: hết hàng).
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deductIngredientsForOrder(Order order) {
        if (order.getOrderDetails() == null) {
            return; // Không có gì để trừ
        }

        // Lặp qua từng chi tiết đơn hàng
        for (OrderDetail detail : order.getOrderDetails()) {
            // Chỉ xử lý các item chưa bị xóa
            if (detail.getIsDeleted() == null || !detail.getIsDeleted()) {
                Product product = detail.getProduct();
                int quantitySold = detail.getQuantity();

                if (product != null) {
                    // Trừ NVL cho sản phẩm chính
                    deductForProduct(product, quantitySold);
                }

                // (Mở rộng: Nếu OrderDetail có lưu topping, cũng trừ kho topping ở đây)
                // Ví dụ: if (detail.getOrderToppings() != null) { ... }
            }
        }
    }

    /**
     * Hoàn kho cho đơn hàng bị hủy
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void restockIngredientsForCancelledOrder(Order order) {
        if (order.getOrderDetails() == null) {
            return;
        }

        for (OrderDetail detail : order.getOrderDetails()) {
            if (detail.getIsDeleted() == null || !detail.getIsDeleted()) {
                Product product = detail.getProduct();
                int quantityToRestock = detail.getQuantity();

                if (product != null) {
                    // Hoàn kho (số lượng âm)
                    deductForProduct(product, -quantityToRestock);
                }
                // (Tương tự, hoàn kho cho toppings)
            }
        }
    }


    /**
     * Hàm helper để trừ kho cho 1 sản phẩm
     * @param product Sản phẩm (phải được EAGER load ProductIngredients)
     * @param quantitySold Số lượng bán (có thể là số âm để hoàn kho)
     */
    private void deductForProduct(Product product, int quantitySold) {
        // ❗ FIX NOTE: Đảm bảo productIngredients đã load (nếu LAZY). Nếu không, dùng EntityGraph trong repo.
        // Ví dụ: product = productRepository.findById(product.getProductId(), entityGraph);
        Set<ProductIngredient> ingredientsNeeded = product.getProductIngredients(); // Sẽ load lazy nếu chưa

        for (ProductIngredient pi : ingredientsNeeded) {
            Ingredient ingredient = pi.getIngredient();
            BigDecimal quantityToDeduct = pi.getQuantityUsed().multiply(BigDecimal.valueOf(quantitySold));

            // Lấy tồn kho hiện tại
            BigDecimal currentStock = ingredient.getStockQuantity();

            // Kiểm tra tồn kho (chỉ kiểm tra nếu là trừ kho)
            if (quantityToDeduct.compareTo(BigDecimal.ZERO) > 0 && currentStock.compareTo(quantityToDeduct) < 0) {
                // Không đủ hàng
                throw new RuntimeException("Không đủ hàng tồn kho cho NVL: " + ingredient.getName());
            }

            // Trừ kho (hoặc cộng kho nếu quantityToDeduct là số âm)
            ingredient.setStockQuantity(currentStock.subtract(quantityToDeduct));
            ingredientRepository.save(ingredient);
        }

        // (Lặp lại logic tương tự cho Topping)
        // Set<ToppingIngredient> toppingIngredientsNeeded = ...
    }
}