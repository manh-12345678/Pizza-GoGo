package Group5_pizza.Pizza_GoGo.DTO;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OrderReviewForm {
    private Integer orderId;
    private List<OrderReviewItem> items = new ArrayList<>();

    @Data
    public static class OrderReviewItem {
        private Integer reviewId;
        private Integer orderDetailId;
        private Integer productId;
        private String productName;
        private Integer quantity;
        private Integer rating;
        private String comment;
        private boolean alreadyReviewed;
    }
}


