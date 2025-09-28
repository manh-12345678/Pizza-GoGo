package Group5_pizza.Pizza_GoGo.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderId")
    private Integer orderId;

    @ManyToOne
    @JoinColumn(name = "CustomerId")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "TableId")
    private RestaurantTable table;

    @Column(name = "OrderType", nullable = false, length = 20)
    private String orderType;

    @Column(name = "VoucherId")
    private Integer voucherId;

    @Column(name = "TotalAmount", nullable = false, columnDefinition = "DECIMAL(18,2) DEFAULT 0")
    private BigDecimal totalAmount;

    @Column(name = "DepositAmount", nullable = false, columnDefinition = "DECIMAL(18,2) DEFAULT 0")
    private BigDecimal depositAmount;

    @Column(name = "PaidAmount", nullable = false, columnDefinition = "DECIMAL(18,2) DEFAULT 0")
    private BigDecimal paidAmount;

    @Column(name = "Status", nullable = false, length = 20, columnDefinition = "NVARCHAR(20) DEFAULT 'Pending'")
    private String status;

    @Column(name = "CreatedAt", columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "IsDeleted", columnDefinition = "BIT DEFAULT 0")
    private Boolean isDeleted;

    @OneToMany(mappedBy = "order")
    private List<OrderDetail> orderDetails;

    @OneToMany(mappedBy = "order")
    private List<Review> reviews;

    @OneToMany(mappedBy = "order")
    private List<Shipping> shippings;
}