package Group5_pizza.Pizza_GoGo.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "Account")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserId")
    private Integer userId;

    @Column(name = "Username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "PasswordHash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "FullName", length = 100)
    private String fullName;

    @Column(name = "Email", nullable = false, unique = true, length = 100)  // Thêm trường email
    private String email;

    @ManyToOne
    @JoinColumn(name = "RoleId", nullable = false)
    @ToString.Exclude
    @JsonBackReference
    private Role role;

    @Column(name = "CreatedAt", updatable = false, columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @OneToOne
    @JoinColumn(name = "CustomerId", unique = true)
    @ToString.Exclude
    @JsonBackReference
    private Customer customer;

    @Column(name = "IsDeleted", columnDefinition = "BIT DEFAULT 0")
    private Boolean isDeleted = false;

    @Column(name = "IsConfirmed", columnDefinition = "BIT DEFAULT 0")  // Thêm flag xác nhận email
    private Boolean isConfirmed = false;

    public Account(LocalDateTime createdAt, Customer customer, String email, String fullName, String passwordHash, Role role, LocalDateTime updatedAt, Integer userId, String username) {
        this.createdAt = createdAt;
        this.customer = customer;
        this.email = email;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.role = role;
        this.updatedAt = updatedAt;
        this.userId = userId;
        this.username = username;
    }
}