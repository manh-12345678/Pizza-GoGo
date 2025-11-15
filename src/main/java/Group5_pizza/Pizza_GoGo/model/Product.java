// Sửa file: Group5_pizza.Pizza_GoGo.model.Product.java (Thêm @JsonIgnore cho orderDetails để phá cycle)
package Group5_pizza.Pizza_GoGo.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
@Entity
@Table(name = "Products")
@Data // Giữ @Data cho getters/setters
@EqualsAndHashCode(exclude = {"comboDetails", "orderDetails", "productDailyStocks", "productToppings", "productIngredients"}) // ❗ FIX: Exclude collections để tránh cycle hashCode
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductId")
    private Integer productId;
    @ManyToOne
    @JoinColumn(name = "CategoryId", nullable = false)
    @ToString.Exclude // ❗ SỬA LỖI: An toàn
    @JsonBackReference
    private Category category;
    @Column(name = "Name", nullable = false, length = 200)
    private String name;
    @Column(name = "Price", nullable = false)
    private BigDecimal price = BigDecimal.ZERO;
    @Column(name = "QuantityPerDay", nullable = false)
    private Integer quantityPerDay;
    @Column(name = "Description", length = 1000)
    private String description;
    @Column(name = "CreatedAt", updatable = false, columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime createdAt;
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
    @Column(name = "IsDeleted", columnDefinition = "BIT DEFAULT 0")
    private Boolean isDeleted = false;
    @Column(name = "img_url", length = 500)
    private String imgUrl;
    @OneToMany(mappedBy = "product")
    @ToString.Exclude // ❗ SỬA LỖI: Thêm dòng này
    @JsonManagedReference
    private List<ComboDetail> comboDetails;
    @OneToMany(mappedBy = "product")
    @ToString.Exclude // ❗ SỬA LỖI: Thêm dòng này
    @JsonIgnore // ❗ FIX CYCLE: Bỏ serialize orderDetails từ Product
    private List<OrderDetail> orderDetails;
    @OneToMany(mappedBy = "product")
    @ToString.Exclude // ❗ SỬA LỖI: Thêm dòng này
    @JsonManagedReference
    private List<ProductDailyStock> productDailyStocks;
    @OneToMany(mappedBy = "product")
    @ToString.Exclude // ❗ SỬA LỖI: Thêm dòng này
    @JsonManagedReference
    private List<ProductTopping> productToppings = new ArrayList<>(); // Vẫn giữ khởi tạo
    // ❗ FIX: Đổi EAGER → LAZY để tránh StackOverflow (load lazy khi cần)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude // ❗ SỬA LỖI: Thêm dòng này
    @JsonManagedReference
    private Set<ProductIngredient> productIngredients = new HashSet<>();
    public List<Topping> getToppings() {
        if (productToppings == null || productToppings.isEmpty()) {
            return List.of();
        }
        return productToppings.stream()
                .filter(pt -> pt != null && pt.getTopping() != null)
                .map(ProductTopping::getTopping)
                .filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()))
                .toList();
    }
}