package Group5_pizza.Pizza_GoGo.DTO;

// Bỏ validation imports
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private Integer reviewId;

    private Integer userId;

    private Integer orderId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}