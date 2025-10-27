package Group5_pizza.Pizza_GoGo.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List; // Import này có thể xóa nếu không dùng List ở đâu khác trong file này

@Entity
@Table(name = "Customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustomerId")
    private Integer customerId;

    @Column(name = "Name", nullable = false, length = 100)
    private String name;

    @Column(name = "Phone", unique = true, length = 20)
    private String phone;

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "Points", nullable = false)
    private Integer points = 0;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "IsDeleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonManagedReference
    private List<Order> orders;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonManagedReference
    private Account account;


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (points == null) points = 0;
        if (isDeleted == null) isDeleted = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}