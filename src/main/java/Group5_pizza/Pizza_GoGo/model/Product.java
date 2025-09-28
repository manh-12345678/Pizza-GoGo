package Group5_pizza.Pizza_GoGo.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductId")
    private Integer productId;

    @ManyToOne
    @JoinColumn(name = "CategoryId", nullable = false)
    private Category category;

    @Column(name = "Name", nullable = false, length = 200)
    private String name;

    @Column(name = "Price", nullable = false, columnDefinition = "DECIMAL(18,2)")
    private BigDecimal price;

    @Column(name = "QuantityPerDay", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer quantityPerDay;

    @Column(name = "Description", length = 1000)
    private String description;

    @Column(name = "CreatedAt", columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "IsDeleted", columnDefinition = "BIT DEFAULT 0")
    private Boolean isDeleted;

    @OneToMany(mappedBy = "product")
    private List<ComboDetail> comboDetails;

    @OneToMany(mappedBy = "product")
    private List<OrderDetail> orderDetails;

    @OneToMany(mappedBy = "product")
    private List<ProductDailyStock> productDailyStocks;

    @OneToMany(mappedBy = "product")
    private List<ProductTopping> productToppings;
}