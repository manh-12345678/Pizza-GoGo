package Group5_pizza.Pizza_GoGo.service.impl;

import Group5_pizza.Pizza_GoGo.DTO.OrderReviewForm;
import Group5_pizza.Pizza_GoGo.DTO.ReviewDTO;
import Group5_pizza.Pizza_GoGo.DTO.ReviewView;
import Group5_pizza.Pizza_GoGo.model.Account;
import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.OrderDetail;
import Group5_pizza.Pizza_GoGo.model.Product;
import Group5_pizza.Pizza_GoGo.model.Review;
import Group5_pizza.Pizza_GoGo.model.enums.ReviewStatus;
import Group5_pizza.Pizza_GoGo.repository.AccountRepository;
import Group5_pizza.Pizza_GoGo.repository.OrderDetailRepository;
import Group5_pizza.Pizza_GoGo.repository.OrderRepository;
import Group5_pizza.Pizza_GoGo.repository.ReviewRepository;
import Group5_pizza.Pizza_GoGo.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private static final Set<String> POSITIVE_ORDER_STATUSES = Set.of("COMPLETED", "DELIVERED", "PAID");

    private final ReviewRepository reviewRepository;
    private final AccountRepository accountRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Override
    @Transactional(readOnly = true)
    public OrderReviewForm buildOrderReviewForm(Integer orderId, Account account) {
        Order order = loadOrderWithDetails(orderId);
        ensureCanReview(order, account);

        OrderReviewForm form = new OrderReviewForm();
        form.setOrderId(orderId);

        Map<Integer, Review> existingReviews = reviewRepository.findAllForOrder(orderId).stream()
                .filter(r -> r.getAccount() != null && account != null &&
                        Objects.equals(r.getAccount().getUserId(), account.getUserId()))
                .collect(Collectors.toMap(r -> r.getOrderDetail().getOrderDetailId(), r -> r, (a, b) -> a));

        order.getOrderDetails().stream()
                .filter(detail -> detail != null && !Boolean.TRUE.equals(detail.getIsDeleted()))
                .forEach(detail -> {
                    OrderReviewForm.OrderReviewItem item = new OrderReviewForm.OrderReviewItem();
                    item.setOrderDetailId(detail.getOrderDetailId());
                    Product product = detail.getProduct();
                    item.setProductId(product != null ? product.getProductId() : null);
                    item.setProductName(product != null ? product.getName() : "Sản phẩm");
                    item.setQuantity(detail.getQuantity());

                    Review existing = existingReviews.get(detail.getOrderDetailId());
                    if (existing != null) {
                        item.setReviewId(existing.getReviewId());
                        item.setRating(existing.getRating());
                        item.setComment(existing.getComment());
                        item.setAlreadyReviewed(existing.isPublic());
                    }
                    form.getItems().add(item);
                });
        return form;
    }

    @Override
    @Transactional
    public void submitOrderReviews(OrderReviewForm form, Account account) {
        if (form == null || form.getOrderId() == null) {
            throw new IllegalArgumentException("Thiếu thông tin đơn hàng cần đánh giá");
        }
        Order order = loadOrderWithDetails(form.getOrderId());
        ensureCanReview(order, account);

        if (form.getItems() == null || form.getItems().isEmpty()) {
            throw new IllegalArgumentException("Không có sản phẩm nào để đánh giá");
        }

        int savedCount = 0;
        for (OrderReviewForm.OrderReviewItem item : form.getItems()) {
            if (item == null) {
                continue;
            }
            try {
                saveReviewItem(order, item, account);
                if (item.getRating() != null && item.getRating() >= 1 && item.getRating() <= 5) {
                    savedCount++;
                }
            } catch (Exception e) {
                System.err.println("Error saving review item: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        if (savedCount == 0) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một đánh giá (sao) cho sản phẩm");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewView> getOrderReviewsForCustomer(Integer orderId, Account account) {
        List<Review> reviews = reviewRepository.findAllForOrder(orderId);
        boolean isManager = isManager(account);
        return reviews.stream()
                .filter(review -> {
                    if (review.getIsDeleted()) {
                        return false;
                    }
                    if (isManager) {
                        return true;
                    }
                    if (account != null && review.getAccount() != null &&
                            Objects.equals(review.getAccount().getUserId(), account.getUserId())) {
                        return true;
                    }
                    return review.getStatus() == ReviewStatus.PUBLISHED;
                })
                .map(this::toView)
                .sorted(Comparator.comparing(ReviewView::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewView> getPublishedReviewsForProduct(Integer productId) {
        return reviewRepository.findByProductProductIdAndStatusAndIsDeletedFalse(productId, ReviewStatus.PUBLISHED)
                .stream()
                .map(this::toView)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewView> getReviewsForManagement() {
        List<ReviewStatus> statuses = Arrays.asList(
                ReviewStatus.PENDING,
                ReviewStatus.PUBLISHED,
                ReviewStatus.HIDDEN,
                ReviewStatus.BLOCKED
        );
        return reviewRepository.findByStatusInOrderByCreatedAtDesc(statuses)
                .stream()
                .map(this::toView)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean updateReviewStatus(Integer reviewId, ReviewStatus status) {
        if (reviewId == null || status == null) {
            return false;
        }
        return reviewRepository.findById(reviewId).map(review -> {
            review.setStatus(status);
            if (status == ReviewStatus.BLOCKED) {
                review.setIsFlaggedSpam(true);
            }
            if (status == ReviewStatus.PUBLISHED) {
                review.setIsDeleted(false);
                review.setIsFlaggedSpam(false);
            }
            if (status == ReviewStatus.HIDDEN) {
                review.setIsFlaggedSpam(false);
            }
            reviewRepository.save(review);
            return true;
        }).orElse(false);
    }

    @Override
    @Transactional
    public boolean toggleSpamFlag(Integer reviewId, boolean flagged) {
        if (reviewId == null) {
            return false;
        }
        return reviewRepository.findById(reviewId).map(review -> {
            review.setIsFlaggedSpam(flagged);
            if (flagged) {
                review.setStatus(ReviewStatus.BLOCKED);
            } else if (review.getStatus() == ReviewStatus.BLOCKED) {
                review.setStatus(ReviewStatus.PUBLISHED);
            }
            reviewRepository.save(review);
            return true;
        }).orElse(false);
    }

    @Override
    @Transactional
    public boolean restoreReview(Integer reviewId) {
        return updateReviewStatus(reviewId, ReviewStatus.PUBLISHED);
    }

    @Override
    @Transactional
    public boolean deleteReview(Integer reviewId) {
        if (reviewId == null) {
            return false;
        }
        return reviewRepository.findById(reviewId).map(review -> {
            review.setIsDeleted(true);
            review.setStatus(ReviewStatus.BLOCKED);
            review.setIsFlaggedSpam(true);
            reviewRepository.save(review);
            return true;
        }).orElse(false);
    }

    @Override
    @Transactional
    public Review respondToReview(Integer reviewId, String reply, Account admin) {
        if (reviewId == null) {
            throw new IllegalArgumentException("Thiếu mã đánh giá");
        }
        return reviewRepository.findById(reviewId).map(review -> {
            review.setAdminReply(StringUtils.hasText(reply) ? reply.trim() : null);
            review.setAdminReplyAt(LocalDateTime.now());
            review.setStatus(ReviewStatus.PUBLISHED);
            review.setIsDeleted(false);
            review.setIsFlaggedSpam(false);
            if (admin != null) {
                review.setAdminResponder(resolveAccountName(admin));
            }
            return reviewRepository.save(review);
        }).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đánh giá"));
    }

    @Override
    @Transactional
    public Review addOrUpdateReview(ReviewDTO reviewDTO, Account account) {
        if (reviewDTO == null) {
            throw new IllegalArgumentException("Thiếu dữ liệu đánh giá");
        }
        Account author = account;
        if (author == null) {
            Integer userId = reviewDTO.getUserId();
            author = userId != null ? accountRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản")) : null;
        }
        if (author == null) {
            throw new IllegalStateException("Cần xác định tài khoản người đánh giá");
        }
        Integer orderId = reviewDTO.getOrderId();
        if (orderId == null) {
            throw new IllegalArgumentException("Thiếu mã đơn hàng");
        }
        Order order = loadOrderWithDetails(orderId);
        ensureCanReview(order, author);

        OrderDetail orderDetail = resolveOrderDetail(order, reviewDTO.getOrderDetailId(), reviewDTO.getProductId());
        return saveOrUpdateReviewEntity(order, orderDetail, reviewDTO, author);
    }

    private Review saveOrUpdateReviewEntity(Order order,
                                            OrderDetail orderDetail,
                                            ReviewDTO reviewDTO,
                                            Account author) {
        // Rating là bắt buộc (NOT NULL trong database)
        // Nếu không có rating hợp lệ, bỏ qua review này
        if (reviewDTO.getRating() == null || reviewDTO.getRating() < 1 || reviewDTO.getRating() > 5) {
            // Nếu không có rating hợp lệ, bỏ qua (không throw exception)
            return null;
        }

        Review review = reviewRepository
                .findByOrderDetailOrderDetailIdAndAccountUserId(orderDetail.getOrderDetailId(), author.getUserId())
                .orElseGet(Review::new);

        boolean isNew = review.getReviewId() == null;
        review.setAccount(author);
        review.setOrder(order);
        review.setOrderDetail(orderDetail);
        review.setProduct(orderDetail.getProduct());
        review.setRating(clampRating(reviewDTO.getRating())); // Rating luôn có giá trị hợp lệ ở đây
        review.setComment(StringUtils.hasText(reviewDTO.getComment()) ? reviewDTO.getComment().trim() : null);
        review.setStatus(ReviewStatus.PUBLISHED);
        review.setIsDeleted(false);
        review.setIsFlaggedSpam(false);
        if (isNew) {
            review.setCreatedAt(LocalDateTime.now());
        }
        return reviewRepository.save(review);
    }

    private void saveReviewItem(Order order, OrderReviewForm.OrderReviewItem item, Account account) {
        // Rating là bắt buộc - bỏ qua nếu không có rating hợp lệ
        if (item.getRating() == null || item.getRating() < 1 || item.getRating() > 5) {
            return;
        }
        ReviewDTO dto = new ReviewDTO();
        dto.setOrderId(order.getOrderId());
        dto.setOrderDetailId(item.getOrderDetailId());
        dto.setProductId(item.getProductId());
        dto.setRating(item.getRating());
        dto.setComment(StringUtils.hasText(item.getComment()) ? item.getComment().trim() : null);
        dto.setUserId(account != null ? account.getUserId() : null);
        Review saved = saveOrUpdateReviewEntity(order, resolveOrderDetail(order, item.getOrderDetailId(), item.getProductId()), dto, account);
        // Nếu saveOrUpdateReviewEntity trả về null (không có dữ liệu), bỏ qua
        if (saved == null) {
            return;
        }
    }

    private Order loadOrderWithDetails(Integer orderId) {
        return orderRepository.findByIdWithDetailsAndToppings(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng #" + orderId));
    }

    private OrderDetail resolveOrderDetail(Order order, Integer orderDetailId, Integer productId) {
        if (orderDetailId != null) {
            return order.getOrderDetails().stream()
                    .filter(detail -> Objects.equals(detail.getOrderDetailId(), orderDetailId))
                    .findFirst()
                    .orElseGet(() -> {
                        OrderDetail detail = orderDetailRepository.findById(orderDetailId)
                                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi tiết đơn hàng"));
                        if (detail.getOrder() == null || !Objects.equals(detail.getOrder().getOrderId(), order.getOrderId())) {
                            throw new IllegalArgumentException("Chi tiết đơn hàng không thuộc về đơn hàng này");
                        }
                        return detail;
                    });
        }
        if (productId != null) {
            return order.getOrderDetails().stream()
                    .filter(detail -> detail.getProduct() != null
                            && Objects.equals(detail.getProduct().getProductId(), productId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm trong đơn hàng"));
        }
        throw new IllegalArgumentException("Thiếu thông tin chi tiết sản phẩm cần đánh giá");
    }

    private void ensureCanReview(Order order, Account account) {
        if (order == null) {
            throw new IllegalArgumentException("Đơn hàng không hợp lệ");
        }
        if (!isManager(account)) {
            if (account == null) {
                throw new IllegalStateException("Vui lòng đăng nhập để đánh giá");
            }
            
            // Kiểm tra quyền: Order phải thuộc về account này
            boolean hasPermission = false;
            
            // Trường hợp 1: Order có account trực tiếp - so sánh accountId
            if (order.getAccount() != null && account.getUserId() != null) {
                hasPermission = Objects.equals(order.getAccount().getUserId(), account.getUserId());
            }
            
            // Trường hợp 2: Order có customer và account có customer - so sánh customerId
            if (!hasPermission && order.getCustomer() != null && account.getCustomer() != null) {
                hasPermission = Objects.equals(order.getCustomer().getCustomerId(), account.getCustomer().getCustomerId());
            }
            
            // Trường hợp 3: Order có account và account đó trỏ đến customer của account hiện tại
            if (!hasPermission && order.getAccount() != null && order.getAccount().getCustomer() != null 
                    && account.getCustomer() != null) {
                hasPermission = Objects.equals(order.getAccount().getCustomer().getCustomerId(), account.getCustomer().getCustomerId());
            }
            
            if (!hasPermission) {
                throw new IllegalArgumentException("Bạn không có quyền đánh giá đơn hàng này");
            }
        }
        if (!POSITIVE_ORDER_STATUSES.contains(order.getStatus() != null ? order.getStatus().toUpperCase() : "")) {
            throw new IllegalStateException("Đơn hàng chưa hoàn tất nên chưa thể đánh giá");
        }
    }

    private boolean isManager(Account account) {
        if (account == null || account.getRole() == null || !StringUtils.hasText(account.getRole().getRoleName())) {
            return false;
        }
        String role = account.getRole().getRoleName().toUpperCase();
        return role.contains("ADMIN") || role.contains("STAFF");
    }

    private ReviewView toView(Review review) {
        String productName = null;
        if (review.getProduct() != null) {
            productName = review.getProduct().getName();
        } else if (review.getOrderDetail() != null && review.getOrderDetail().getProduct() != null) {
            productName = review.getOrderDetail().getProduct().getName();
        }

        return ReviewView.builder()
                .reviewId(review.getReviewId())
                .orderId(review.getOrder() != null ? review.getOrder().getOrderId() : null)
                .orderDetailId(review.getOrderDetail() != null ? review.getOrderDetail().getOrderDetailId() : null)
                .productId(review.getProduct() != null ? review.getProduct().getProductId() : null)
                .productName(productName)
                .rating(review.getRating())
                .comment(review.getComment())
                .customerName(resolveAccountName(review.getAccount()))
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .status(review.getStatus())
                .flaggedSpam(Boolean.TRUE.equals(review.getIsFlaggedSpam()))
                .adminReply(review.getAdminReply())
                .adminReplyAt(review.getAdminReplyAt())
                .adminResponder(review.getAdminResponder())
                .deleted(Boolean.TRUE.equals(review.getIsDeleted()))
                .build();
    }

    private String resolveAccountName(Account account) {
        if (account == null) {
            return "Người dùng";
        }
        if (StringUtils.hasText(account.getFullName())) {
            return account.getFullName();
        }
        if (StringUtils.hasText(account.getUsername())) {
            return account.getUsername();
        }
        return "Người dùng #" + account.getUserId();
    }

    private int clampRating(Integer rating) {
        if (rating == null) {
            return 5;
        }
        return Math.max(1, Math.min(5, rating));
    }
}
