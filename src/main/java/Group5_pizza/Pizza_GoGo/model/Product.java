package Group5_pizza.Pizza_GoGo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    @ToString.Exclude
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
    @ToString.Exclude
    @JsonManagedReference
    private List<ComboDetail> comboDetails;

    @OneToMany(mappedBy = "product")
    @ToString.Exclude
    @JsonManagedReference
    private List<OrderDetail> orderDetails;

    @OneToMany(mappedBy = "product")
    @ToString.Exclude
    @JsonManagedReference
    private List<ProductDailyStock> productDailyStocks;

    @OneToMany(mappedBy = "product")
    @ToString.Exclude
    @JsonManagedReference
    private List<ProductTopping> productToppings;
}