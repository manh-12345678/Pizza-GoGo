// package Group5_pizza.Pizza_GoGo.service;
// ProductService.java
package Group5_pizza.Pizza_GoGo.service;
import java.util.List;
import Group5_pizza.Pizza_GoGo.DTO.ProductDTO;
import Group5_pizza.Pizza_GoGo.model.Product;
public interface ProductService {
    // Tìm kiếm và lọc
    List<ProductDTO> searchAndFilterProducts(String search, Integer categoryId);
    // Lấy DTO (cho form)
    ProductDTO getProductDTOById(Integer id);
    // Lấy Entity (cho nội bộ)
    Product getProductById(Integer id);
    // Lấy Entity với toppings (cho API)
    Product getProductByIdWithToppings(Integer id);
    // ❗ SỬA LỖI: Đổi tên hàm để khớp với Impl
    Product createProductWithDetails(ProductDTO productDTO);
    // ❗ SỬA LỖI: Đổi tên hàm để khớp với Impl
    Product updateProductWithDetails(Integer id, ProductDTO productDTO);
    // Xóa
    void deleteProduct(Integer id);
    // Lấy tất cả
    List<Product> getAllProducts();
    List<ProductDTO> getAllProductsForOrder();
}