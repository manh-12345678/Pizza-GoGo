package Group5_pizza.Pizza_GoGo.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Toppings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Topping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ToppingId")
    private Integer toppingId;

    @Column(name = "Name", nullable = false, length = 100)
    private String name;

    @Column(name = "Price", nullable = false, columnDefinition = "DECIMAL(18,2) DEFAULT 0")
    private BigDecimal price;

    @Column(name = "CreatedAt", columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "IsDeleted", columnDefinition = "BIT DEFAULT 0")
    private Boolean isDeleted;

    @OneToMany(mappedBy = "topping")
    private List<ProductTopping> productToppings;
}