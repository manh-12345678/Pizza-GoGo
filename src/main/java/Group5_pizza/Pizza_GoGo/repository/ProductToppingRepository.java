// package Group5_pizza.Pizza_GoGo.repository;
// ProductToppingRepository.java
package Group5_pizza.Pizza_GoGo.repository;
import Group5_pizza.Pizza_GoGo.model.Product;
import Group5_pizza.Pizza_GoGo.model.ProductTopping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List; // ❗ THÊM IMPORT
@Repository
public interface ProductToppingRepository extends JpaRepository<ProductTopping, Integer> {
    // ❗ HÀM MỚI (TỪ BÀI HỌC TRƯỚC): Xóa tất cả các liên kết bởi đối tượng Product
    // Hàm này được ProductServiceImpl sử dụng
    @Transactional
    void deleteByProduct(Product product);
    // ❗ HÀM MỚI (ĐỂ SỬA LỖI): Xóa tất cả các liên kết bởi Product ID
    // Hàm này được ProductToppingServiceImpl sử dụng
    @Transactional
    void deleteByProduct_ProductId(Integer productId);
    // Hàm tìm kiếm (đã có trong file ServiceImpl của em)
    List<ProductTopping> findByProduct_ProductId(Integer productId);
}