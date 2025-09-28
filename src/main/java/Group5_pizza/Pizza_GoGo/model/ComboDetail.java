package Group5_pizza.Pizza_GoGo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ComboDetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComboDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ComboDetailId")
    private Integer comboDetailId;

    @ManyToOne
    @JoinColumn(name = "ComboId", nullable = false)
    private Combo combo;

    @ManyToOne
    @JoinColumn(name = "ProductId", nullable = false)
    private Product product;

    @Column(name = "Quantity", nullable = false, columnDefinition = "INT DEFAULT 1")
    private Integer quantity;

    @Column(name = "ComboId", insertable = false, updatable = false)
    private Integer comboId;

    @Column(name = "ProductId", insertable = false, updatable = false)
    private Integer productId;
}