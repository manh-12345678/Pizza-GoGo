package Group5_pizza.Pizza_GoGo.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Combos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Combo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ComboId")
    private Integer comboId;

    @Column(name = "Name", nullable = false, length = 150)
    private String name;

    @Column(name = "DiscountPercent", nullable = false, columnDefinition = "DECIMAL(5,2)")
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "StartDate")
    private LocalDateTime startDate;

    @Column(name = "EndDate")
    private LocalDateTime endDate;

    @Column(name = "CreatedAt", updatable = false, columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "IsDeleted", columnDefinition = "BIT DEFAULT 0")
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "combo")
    @ToString.Exclude
    @JsonManagedReference
    private List<ComboDetail> comboDetails;
}