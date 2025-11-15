package Group5_pizza.Pizza_GoGo.repository;

import Group5_pizza.Pizza_GoGo.model.Review;
import Group5_pizza.Pizza_GoGo.model.enums.ReviewStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    @EntityGraph(attributePaths = {"orderDetail", "orderDetail.product", "product", "account"})
    List<Review> findByOrderOrderIdAndStatusNotAndIsDeletedFalse(Integer orderId, ReviewStatus status);

    @EntityGraph(attributePaths = {"orderDetail", "orderDetail.product", "product", "account"})
    List<Review> findByAccountUserIdAndStatusNotAndIsDeletedFalse(Integer userId, ReviewStatus status);

    @EntityGraph(attributePaths = {"orderDetail", "orderDetail.product", "product", "account"})
    List<Review> findByProductProductIdAndStatusAndIsDeletedFalse(Integer productId, ReviewStatus status);

    @EntityGraph(attributePaths = {"orderDetail", "orderDetail.product", "product", "account"})
    List<Review> findByStatusInOrderByCreatedAtDesc(Collection<ReviewStatus> statuses);

    Optional<Review> findByOrderDetailOrderDetailIdAndAccountUserId(Integer orderDetailId, Integer userId);

    @Query("select r from Review r " +
            "left join fetch r.orderDetail od " +
            "left join fetch r.product p " +
            "left join fetch r.account a " +
            "where r.order.orderId = :orderId")
    List<Review> findAllForOrder(@Param("orderId") Integer orderId);
}
