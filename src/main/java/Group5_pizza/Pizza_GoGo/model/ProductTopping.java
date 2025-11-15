// package Group5_pizza.Pizza_GoGo.model;
// ProductTopping.java
package Group5_pizza.Pizza_GoGo.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "ProductToppings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductTopping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductToppingId")
    private Integer productToppingId;
    @ManyToOne(fetch = FetchType.LAZY) // Thêm FetchType
    @JoinColumn(name = "ProductId", nullable = false)
    @ToString.Exclude // ❗ SỬA LỖI: Thêm dòng này
    @JsonBackReference
    private Product product;
    @ManyToOne(fetch = FetchType.LAZY) // Thêm FetchType
    @JoinColumn(name = "ToppingId", nullable = false)
    @ToString.Exclude // ❗ SỬA LỖI: Thêm dòng này
    @JsonBackReference
    private Topping topping;
}