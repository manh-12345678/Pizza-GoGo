package Group5_pizza.Pizza_GoGo.DTO;

import Group5_pizza.Pizza_GoGo.model.enums.ReviewStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewView {
    private Integer reviewId;
    private Integer orderId;
    private Integer orderDetailId;
    private Integer productId;
    private String productName;
    private Integer rating;
    private String comment;
    private String customerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ReviewStatus status;
    private boolean flaggedSpam;
    private String adminReply;
    private LocalDateTime adminReplyAt;
    private String adminResponder;
    private boolean deleted;
}


