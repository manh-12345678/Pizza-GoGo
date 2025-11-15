package Group5_pizza.Pizza_GoGo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import Group5_pizza.Pizza_GoGo.model.enums.ReviewStatus;

@Entity
@Table(name = "Reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReviewId")
    private Integer reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId")
    @ToString.Exclude
    @JsonBackReference
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderId")
    @ToString.Exclude
    @JsonBackReference
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderDetailId")
    @ToString.Exclude
    @JsonBackReference
    private OrderDetail orderDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductId")
    @ToString.Exclude
    @JsonBackReference
    private Product product;

    @Column(name = "Rating", nullable = false)
    private Integer rating;

    @Column(name = "Comment", length = 1000)
    private String comment;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "IsDeleted", nullable = false)
    private Boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 20)
    private ReviewStatus status = ReviewStatus.PUBLISHED;

    @Column(name = "AdminReply", length = 1000)
    private String adminReply;

    @Column(name = "AdminReplyAt")
    private LocalDateTime adminReplyAt;

    @Column(name = "AdminResponder", length = 100)
    private String adminResponder;

    @Column(name = "IsFlaggedSpam", nullable = false)
    private Boolean isFlaggedSpam = false;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
        if (status == null) {
            status = ReviewStatus.PENDING;
        }
        if (isFlaggedSpam == null) {
            isFlaggedSpam = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = ReviewStatus.PENDING;
        }
    }

    @JsonIgnore
    public boolean isPublic() {
        return status == ReviewStatus.PUBLISHED && !Boolean.TRUE.equals(isDeleted);
    }
}
