package Group5_pizza.Pizza_GoGo.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Tables")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TableId")
    private Integer tableId;

    @Column(name = "TableName", nullable = false, length = 50)
    private String tableName;

    @Column(name = "TableNumber", nullable = false)
    private Integer tableNumber;

    @Column(name = "Capacity", nullable = false, columnDefinition = "INT DEFAULT 1")
    private Integer capacity = 1;

    @Column(name = "QRCodeUrl", length = 1000)
    private String qrCodeUrl;

    @Column(name = "Status", nullable = false, length = 20, columnDefinition = "NVARCHAR(20) DEFAULT 'Available'")
    private String status = "Available";

    @Column(name = "CreatedAt", updatable = false, columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "IsDeleted", columnDefinition = "BIT DEFAULT 0")
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "table")
    @ToString.Exclude
    @JsonManagedReference
    private List<Order> orders;
}