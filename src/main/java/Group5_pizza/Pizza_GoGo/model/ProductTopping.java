package Group5_pizza.Pizza_GoGo.model;

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

    @ManyToOne
    @JoinColumn(name = "ProductId", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "ToppingId", nullable = false)
    private Topping topping;

    @Column(name = "ProductId", insertable = false, updatable = false)
    private Integer productId;

    @Column(name = "ToppingId", insertable = false, updatable = false)
    private Integer toppingId;
}