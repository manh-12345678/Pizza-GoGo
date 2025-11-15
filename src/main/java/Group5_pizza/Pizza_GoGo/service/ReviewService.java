package Group5_pizza.Pizza_GoGo.service;

import Group5_pizza.Pizza_GoGo.DTO.OrderReviewForm;
import Group5_pizza.Pizza_GoGo.DTO.ReviewDTO;
import Group5_pizza.Pizza_GoGo.DTO.ReviewView;
import Group5_pizza.Pizza_GoGo.model.Account;
import Group5_pizza.Pizza_GoGo.model.Review;
import Group5_pizza.Pizza_GoGo.model.enums.ReviewStatus;

import java.util.List;

public interface ReviewService {

    OrderReviewForm buildOrderReviewForm(Integer orderId, Account account);

    void submitOrderReviews(OrderReviewForm form, Account account);

    List<ReviewView> getOrderReviewsForCustomer(Integer orderId, Account account);

    List<ReviewView> getPublishedReviewsForProduct(Integer productId);

    List<ReviewView> getReviewsForManagement();

    boolean updateReviewStatus(Integer reviewId, ReviewStatus status);

    boolean toggleSpamFlag(Integer reviewId, boolean flagged);

    boolean restoreReview(Integer reviewId);

    boolean deleteReview(Integer reviewId);

    Review respondToReview(Integer reviewId, String reply, Account admin);

    Review addOrUpdateReview(ReviewDTO reviewDTO, Account account);
}
