package Group5_pizza.Pizza_GoGo.controller;

import Group5_pizza.Pizza_GoGo.DTO.ReviewDTO;
import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.Review;
import Group5_pizza.Pizza_GoGo.repository.ReviewRepository;
import Group5_pizza.Pizza_GoGo.service.OrderService;
import Group5_pizza.Pizza_GoGo.service.ReviewService;
// Bỏ validation imports
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
// Bỏ validation imports
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
// Bỏ validation imports

/**
 * Controller xử lý Review, sử dụng Account thay Customer.
 */
@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final OrderService orderService;
    private final ReviewRepository reviewRepository;

    // == Dành cho USER ==

    @GetMapping("/add/{orderId}")
    public String showReviewForm(@PathVariable Integer orderId, Model model) {
        try {
            // Kiểm tra xem Order có tồn tại không (tùy chọn nhưng nên có)
            Order order = orderService.getOrderById(orderId);
            // TODO: Thêm kiểm tra quyền sở hữu order

            // ⭐ SỬA Ở ĐÂY: Tạo DTO và gán orderId vào DTO ⭐
            ReviewDTO reviewDTO = new ReviewDTO();
            reviewDTO.setOrderId(orderId); // Gán ID vào đối tượng DTO

            model.addAttribute("orderId", orderId); // Vẫn giữ lại nếu cần hiển thị ở đâu đó
            model.addAttribute("reviewDTO", reviewDTO); // Truyền DTO đã có orderId ra view

            return "reviews/add_review_form"; // Tên file HTML
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Không tìm thấy đơn hàng.");
            return "error_page"; // Hoặc trang lỗi chung
        }
    }
    @PostMapping("/add")
    public String submitReview(@ModelAttribute("reviewDTO") ReviewDTO reviewDTO,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        // --- Sử dụng UserId cố định để test ---
        Integer currentUserId = 1; // ID user tồn tại trong DB
        reviewDTO.setUserId(currentUserId);
        // --- ---

        // (Tùy chọn) Kiểm tra thủ công
        if (reviewDTO.getRating() == null || reviewDTO.getRating() < 1 || reviewDTO.getRating() > 5) {
            model.addAttribute("orderId", reviewDTO.getOrderId());
            model.addAttribute("errorMessage", "Vui lòng chọn số sao đánh giá (từ 1 đến 5).");
            model.addAttribute("reviewDTO", reviewDTO);
            return "reviews/add_review_form";
        }
        if (reviewDTO.getComment() != null && reviewDTO.getComment().length() > 1000) {
            model.addAttribute("orderId", reviewDTO.getOrderId());
            model.addAttribute("errorMessage", "Bình luận không được vượt quá 1000 ký tự.");
            model.addAttribute("reviewDTO", reviewDTO);
            return "reviews/add_review_form";
        }
        if (reviewDTO.getOrderId() == null) {
            model.addAttribute("errorMessage", "Lỗi: Không xác định được đơn hàng cần đánh giá.");
            model.addAttribute("reviewDTO", reviewDTO);
            return "reviews/add_review_form";
        }

        try {
            reviewService.addReview(reviewDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Gửi đánh giá thành công!");

            // ⭐ THAY ĐỔI URL CHUYỂN HƯỚNG TẠI ĐÂY ⭐
            // return "redirect:/orders/history"; // URL cũ
            return "redirect:/"; // Ví dụ: Chuyển về trang chủ
            // Hoặc: return "redirect:/orders/details/" + reviewDTO.getOrderId(); // Nếu có trang chi tiết đơn hàng

        } catch (RuntimeException e) {
            System.err.println("Lỗi khi thêm review: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("orderId", reviewDTO.getOrderId());
            model.addAttribute("errorMessage", "Lỗi khi lưu đánh giá: " + e.getMessage());
            model.addAttribute("reviewDTO", reviewDTO);
            return "reviews/add_review_form";
        } catch (Exception e) {
            System.err.println("Lỗi không mong muốn khi thêm review: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("orderId", reviewDTO.getOrderId());
            model.addAttribute("errorMessage", "Lỗi hệ thống không mong muốn.");
            model.addAttribute("reviewDTO", reviewDTO);
            return "reviews/add_review_form";
        }
    }

    @GetMapping("/order/{orderId}")
    public String viewOrderReviews(@PathVariable Integer orderId, Model model) {
        try {
            Order order = orderService.getOrderById(orderId);
            model.addAttribute("order", order);
        } catch (RuntimeException e){ /* Bỏ qua */ }
        List<Review> reviews = reviewService.getReviewsByOrderId(orderId);
        model.addAttribute("reviews", reviews);
        model.addAttribute("orderId", orderId);
        return "reviews/order_reviews_list";
    }


    // == Dành cho ADMIN ==

    @GetMapping("/manage")
    public String manageReviews(Model model) {
        List<Review> allReviews = reviewService.getAllActiveReviews();
        model.addAttribute("reviews", allReviews);
        return "reviews/manage_reviews";
    }

    @PostMapping("/delete/{reviewId}")
    public String deleteReviewByAdmin(@PathVariable Integer reviewId, RedirectAttributes redirectAttributes) {
        // TODO: Thêm kiểm tra quyền admin!
        boolean deleted = reviewService.deleteReview(reviewId);
        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa (ẩn) đánh giá thành công.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy hoặc đánh giá đã bị xóa.");
        }
        return "redirect:/reviews/manage";
    }


    // == API Endpoints (Cập nhật đường dẫn/tham số nếu cần) ==

    @GetMapping("/api/order/{orderId}")
    @ResponseBody
    public ResponseEntity<List<Review>> getReviewsForOrderApi(@PathVariable Integer orderId) {
        List<Review> reviews = reviewService.getReviewsByOrderId(orderId);
        return ResponseEntity.ok(reviews);
    }

    // ⭐ THAY ĐỔI ĐƯỜNG DẪN VÀ THAM SỐ ⭐
    @GetMapping("/api/account/{userId}") // Đổi từ customer/{customerId}
    @ResponseBody
    public ResponseEntity<List<Review>> getReviewsByAccountApi(@PathVariable Integer userId) { // Đổi tên tham số
        List<Review> reviews = reviewService.getReviewsByAccountId(userId); // Gọi service mới
        return ResponseEntity.ok(reviews);
    }

    @DeleteMapping("/api/{reviewId}")
    @ResponseBody
    public ResponseEntity<String> deleteReviewApi(@PathVariable Integer reviewId) {
        // TODO: Thêm kiểm tra quyền
        boolean deleted = reviewService.deleteReview(reviewId);
        if (deleted) {
            return ResponseEntity.ok("Đã xóa đánh giá thành công.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy đánh giá để xóa.");
        }
    }
}