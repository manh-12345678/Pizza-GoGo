// package Group5_pizza.Pizza_GoGo.model;
// ProductDailyStock.java
package Group5_pizza.Pizza_GoGo.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
@Entity
@Table(name = "ProductDailyStock")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDailyStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "StockId")
    private Integer stockId;
    @ManyToOne
    @JoinColumn(name = "ProductId", nullable = false)
    @ToString.Exclude
    @JsonBackReference
    private Product product;
    @Column(name = "StockDate", nullable = false)
    private LocalDate stockDate;
    @Column(name = "QuantityRemaining", nullable = false)
    private Integer quantityRemaining;
}