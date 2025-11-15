package Group5_pizza.Pizza_GoGo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Shipping")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shipping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ShippingId")
    private Integer shippingId;

    @ManyToOne
    @JoinColumn(name = "OrderId", nullable = false)
    @ToString.Exclude
    @JsonBackReference
    private Order order;

    @Column(name = "Address", nullable = false, length = 300)
    private String address;

    @Column(name = "ContactName", nullable = false, length = 100)
    private String contactName;

    @Column(name = "ContactPhone", nullable = false, length = 20)
    private String contactPhone;

    @Column(name = "ShipperName", length = 100)
    private String shipperName;

    @Column(name = "ShipperPhone", length = 20)
    private String shipperPhone;

    @Column(name = "Status", nullable = false, length = 20, columnDefinition = "NVARCHAR(20) DEFAULT 'Pending'")
    private String status = "Pending";

    @Column(name = "EstimatedDelivery")
    private LocalDateTime estimatedDelivery;

    @Column(name = "DeliveredAt")
    private LocalDateTime deliveredAt;

    @Column(name = "CreatedAt", updatable = false, columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
}