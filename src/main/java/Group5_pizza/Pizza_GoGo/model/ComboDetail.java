package Group5_pizza.Pizza_GoGo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @ToString.Exclude
    @JsonBackReference
    private Combo combo;

    @ManyToOne
    @JoinColumn(name = "ProductId", nullable = false)
    @ToString.Exclude
    @JsonBackReference
    private Product product;

    @Column(name = "Quantity", nullable = false, columnDefinition = "INT DEFAULT 1")
    private Integer quantity = 1;
}