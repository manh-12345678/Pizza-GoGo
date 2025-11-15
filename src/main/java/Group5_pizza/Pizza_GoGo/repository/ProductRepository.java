// package Group5_pizza.Pizza_GoGo.repository;
// ProductRepository.java
package Group5_pizza.Pizza_GoGo.repository;
import Group5_pizza.Pizza_GoGo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // <-- Đảm bảo bạn đã import
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    /**
     * SỬA LỖI: Thêm LEFT JOIN FETCH cho 'productToppings' và 'pt.topping'.
     *
     * 1. DISTINCT: Ngăn các bản ghi Product bị trùng lặp.
     * 2. LEFT JOIN FETCH p.productToppings pt: Tải ngay lập tức danh sách ProductTopping
     * cho mỗi sản phẩm (Sửa lỗi LazyInitializationException).
     * 3. LEFT JOIN FETCH pt.topping: Tải ngay lập tức thông tin Topping (tên, giá)
     * cho mỗi ProductTopping.
     */
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.productToppings pt " +
            "LEFT JOIN FETCH pt.topping " +
            "WHERE p.isDeleted = false")
    List<Product> findByIsDeletedFalse();

    List<Product> findByNameContainingAndIsDeletedFalse(String name);

    List<Product> findByCategoryCategoryIdAndIsDeletedFalse(Integer categoryId);

    List<Product> findByNameContainingAndCategoryCategoryIdAndIsDeletedFalse(String name, Integer categoryId);
    
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.productToppings pt " +
            "LEFT JOIN FETCH pt.topping " +
            "WHERE p.productId = :productId AND p.isDeleted = false")
    java.util.Optional<Product> findByIdWithToppings(Integer productId);
}