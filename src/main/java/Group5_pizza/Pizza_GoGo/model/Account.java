package Group5_pizza.Pizza_GoGo.model;

import java.time.LocalDateTime;
import java.util.ArrayList; // Thêm import
import java.util.List; // Thêm import

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference; // Thêm import

import jakarta.persistence.*; // Đảm bảo import đầy đủ
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "Account") // Tên bảng nên là Accounts (số nhiều) nếu theo convention
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

    @Column(name = "Email", nullable = false, unique = true, length = 100)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY) // Thêm fetch lazy
    @JoinColumn(name = "RoleId", nullable = false)
    @ToString.Exclude
    @JsonBackReference
    private Role role; // Cần có model Role

    @Column(name = "CreatedAt", nullable = false, updatable = false) // Nên để nullable = false
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    // Quan hệ OneToOne với Customer có thể vẫn giữ nếu cần
    @OneToOne(fetch = FetchType.LAZY) // Thêm fetch lazy
    @JoinColumn(name = "CustomerId", unique = true)
    @ToString.Exclude
    @JsonBackReference
    private Customer customer;

    @Column(name = "IsDeleted", nullable = false) // Nên để nullable = false
    private Boolean isDeleted = false;

    @Column(name = "IsConfirmed", nullable = false) // Nên để nullable = false
    private Boolean isConfirmed = false;

    // ⭐ THÊM MỚI: Liên kết OneToMany tới Review ⭐
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude // Tránh vòng lặp toString
    @JsonManagedReference // Phía "một" của quan hệ
    private List<Review> reviews = new ArrayList<>(); // Khởi tạo list


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if(isDeleted == null) isDeleted = false;
        if(isConfirmed == null) isConfirmed = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}