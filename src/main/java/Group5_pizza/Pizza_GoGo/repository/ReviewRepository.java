package Group5_pizza.Pizza_GoGo.repository;

import Group5_pizza.Pizza_GoGo.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    // Tìm đánh giá theo đơn hàng (chưa bị xóa)
    List<Review> findByOrderOrderIdAndIsDeletedFalse(Integer orderId);

    // ⭐ THAY ĐỔI TỪ CustomerId SANG AccountId ⭐
    // Tìm đánh giá theo tài khoản (chưa bị xóa)
    List<Review> findByAccountUserIdAndIsDeletedFalse(Integer userId); // Đổi tên phương thức và trường

    // (Tùy chọn) Tìm theo rating
    List<Review> findByRatingAndIsDeletedFalse(Integer rating);

    // (Tùy chọn) Tìm tất cả đánh giá chưa bị xóa (cho admin)
    List<Review> findByIsDeletedFalse();
}