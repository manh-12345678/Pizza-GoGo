package Group5_pizza.Pizza_GoGo.service.impl;

import Group5_pizza.Pizza_GoGo.DTO.ReviewDTO;
import Group5_pizza.Pizza_GoGo.model.Account; // ⭐ Import Account
// Bỏ import Customer
import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.Review;
// Bỏ import CustomerRepository
import Group5_pizza.Pizza_GoGo.repository.AccountRepository;
import Group5_pizza.Pizza_GoGo.repository.OrderRepository;
import Group5_pizza.Pizza_GoGo.repository.ReviewRepository;
import Group5_pizza.Pizza_GoGo.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final AccountRepository accountRepository; // ⭐ Inject AccountRepository
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public Review addReview(ReviewDTO reviewDTO) {
        // ⭐ Kiểm tra sự tồn tại của Account
        Account account = accountRepository.findById(reviewDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("Account không tồn tại với ID: " + reviewDTO.getUserId()));

        Order order = orderRepository.findById(reviewDTO.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order không tồn tại với ID: " + reviewDTO.getOrderId()));

        // TODO: (Nâng cao) Kiểm tra xem account này có đúng là chủ của order này không
        // TODO: (Nâng cao) Kiểm tra xem order này đã ở trạng thái được phép review chưa (VD: COMPLETED)
        // TODO: (Nâng cao) Kiểm tra xem order này đã được review bởi account này chưa

        Review review = new Review();
        review.setAccount(account); // ⭐ Gán Account
        review.setOrder(order);
        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());
        review.setCreatedAt(LocalDateTime.now());
        review.setIsDeleted(false);

        return reviewRepository.save(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getReviewsByOrderId(Integer orderId) {
        return reviewRepository.findByOrderOrderIdAndIsDeletedFalse(orderId);
    }

    // ⭐ THAY ĐỔI TÊN VÀ LOGIC PHƯƠNG THỨC ⭐
    @Override
    @Transactional(readOnly = true)
    public List<Review> getReviewsByAccountId(Integer accountId) { // Đổi tên tham số
        return reviewRepository.findByAccountUserIdAndIsDeletedFalse(accountId); // Gọi phương thức mới của repo
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getAllActiveReviews() {
        return reviewRepository.findByIsDeletedFalse();
    }


    @Override
    @Transactional
    public boolean deleteReview(Integer reviewId) {
        Optional<Review> optionalReview = reviewRepository.findById(reviewId);
        if (optionalReview.isPresent() && !optionalReview.get().getIsDeleted()) {
            Review review = optionalReview.get();
            review.setIsDeleted(true);
            reviewRepository.save(review);
            return true;
        }
        return false;
    }
}