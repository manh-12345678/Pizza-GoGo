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

    @ManyToOne
    @JoinColumn(name = "ProductId", nullable = false)
    @ToString.Exclude
    @JsonBackReference
    private Product product;

    @ManyToOne
    @JoinColumn(name = "ToppingId", nullable = false)
    @ToString.Exclude
    @JsonBackReference
    private Topping topping;
}