package Group5_pizza.Pizza_GoGo.model;

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
    private Product product;

    @Column(name = "StockDate", nullable = false)
    private LocalDate stockDate;

    @Column(name = "QuantityRemaining", nullable = false)
    private Integer quantityRemaining;

    @Column(name = "ProductId", insertable = false, updatable = false)
    private Integer productId;
}
