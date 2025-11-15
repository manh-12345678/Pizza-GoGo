package Group5_pizza.Pizza_GoGo.DTO;

// Bỏ validation imports
import Group5_pizza.Pizza_GoGo.model.enums.ReviewStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private Integer reviewId;
    private Integer userId;
    private Integer orderId;
    private Integer orderDetailId;
    private Integer productId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ReviewStatus status;
    private String adminReply;
    private LocalDateTime adminReplyAt;
    private Boolean flaggedSpam;
}
