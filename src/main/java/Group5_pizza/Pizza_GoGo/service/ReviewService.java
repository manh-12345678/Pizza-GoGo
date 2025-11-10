package Group5_pizza.Pizza_GoGo.service;

import Group5_pizza.Pizza_GoGo.DTO.ReviewDTO;
import Group5_pizza.Pizza_GoGo.model.Review;

import java.util.List;

public interface ReviewService {

    Review addReview(ReviewDTO reviewDTO);

    List<Review> getReviewsByOrderId(Integer orderId);

    // THAY ĐỔI TÊN PHƯƠNG THỨC
    List<Review> getReviewsByAccountId(Integer accountId); // Đổi từ CustomerId

    List<Review> getAllActiveReviews();

    boolean deleteReview(Integer reviewId);
}