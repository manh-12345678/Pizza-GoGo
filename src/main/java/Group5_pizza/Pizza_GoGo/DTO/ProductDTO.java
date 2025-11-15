// package Group5_pizza.Pizza_GoGo.DTO;
// ProductDTO.java
package Group5_pizza.Pizza_GoGo.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
/**
 * DTO for Product, including ingredients and available toppings.
 *
 * DÙNG CHO:
 * 1. Quản lý sản phẩm (add/edit)
 * 2. Order Management (API /api/products)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    // === THÔNG TIN CHUNG ===
    private Integer productId;
    private String name;
    private BigDecimal price = BigDecimal.ZERO;
    private Integer quantityPerDay;
    private String description;
    private String imgUrl;
    // === DANH MỤC ===
    private Integer categoryId;
    private String categoryName;
    // === NGUYÊN LIỆU (MÓN ĐƯỢC LÀM TỪ) ===
    private List<ProductIngredientDTO> ingredients;
    // === TOPPING (CÓ THỂ THÊM VÀO) ===
    // Dùng khi tạo/sửa sản phẩm → chỉ cần ID
    private List<Integer> toppingIds;
    // DÙNG CHO ORDER MANAGEMENT (API /api/products)
    // Trả về thông tin topping đầy đủ (tên + giá)
    private List<ToppingDTO> toppings;
    // ==================== NESTED DTO: Topping ====================
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToppingDTO {
        private Integer toppingId;
        private String name;
        private BigDecimal price;
    }
}