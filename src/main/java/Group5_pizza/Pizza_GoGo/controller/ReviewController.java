package Group5_pizza.Pizza_GoGo.controller;

import Group5_pizza.Pizza_GoGo.DTO.OrderReviewForm;
import Group5_pizza.Pizza_GoGo.DTO.ReviewView;
import Group5_pizza.Pizza_GoGo.model.Account;
import Group5_pizza.Pizza_GoGo.model.enums.ReviewStatus;
import Group5_pizza.Pizza_GoGo.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/order/{orderId}/new")
    public String showOrderReviewForm(@PathVariable Integer orderId,
                                      HttpSession session,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        Account loggedInUser = (Account) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để đánh giá sản phẩm.");
            return "redirect:/login";
        }
        try {
            OrderReviewForm form = reviewService.buildOrderReviewForm(orderId, loggedInUser);
            
            // Debug logging
            System.out.println("=== BUILD REVIEW FORM DEBUG ===");
            System.out.println("OrderId: " + orderId);
            System.out.println("Form items count: " + (form.getItems() != null ? form.getItems().size() : 0));
            if (form.getItems() != null) {
                for (int i = 0; i < form.getItems().size(); i++) {
                    OrderReviewForm.OrderReviewItem item = form.getItems().get(i);
                    System.out.println("Item " + i + ": orderDetailId=" + item.getOrderDetailId() 
                        + ", productId=" + item.getProductId() 
                        + ", productName=" + item.getProductName());
                }
            }
            
            model.addAttribute("reviewForm", form);
            model.addAttribute("orderId", orderId);
            return "reviews/add_review_form";
        } catch (Exception ex) {
            ex.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/orders/my-orders";
        }
    }

    @PostMapping("/order/{orderId}")
    public String submitOrderReviews(@PathVariable Integer orderId,
                                     @ModelAttribute("reviewForm") OrderReviewForm reviewForm,
                                     jakarta.servlet.http.HttpServletRequest request,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        Account loggedInUser = (Account) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để gửi đánh giá.");
            return "redirect:/login";
        }
        reviewForm.setOrderId(orderId);
        
        // Debug logging - kiểm tra request parameters
        System.out.println("=== SUBMIT REVIEW DEBUG ===");
        System.out.println("OrderId: " + reviewForm.getOrderId());
        System.out.println("Items count: " + (reviewForm.getItems() != null ? reviewForm.getItems().size() : 0));
        
        // Debug: In tất cả request parameters
        System.out.println("Request parameters:");
        request.getParameterMap().forEach((key, values) -> {
            System.out.println("  " + key + " = " + java.util.Arrays.toString(values));
        });
        
        if (reviewForm.getItems() != null) {
            for (int i = 0; i < reviewForm.getItems().size(); i++) {
                OrderReviewForm.OrderReviewItem item = reviewForm.getItems().get(i);
                System.out.println("Item " + i + ": orderDetailId=" + item.getOrderDetailId() 
                    + ", rating=" + item.getRating() 
                    + ", comment=" + item.getComment());
            }
        } else {
            System.out.println("WARNING: reviewForm.getItems() is NULL!");
        }
        
        try {
            reviewService.submitOrderReviews(reviewForm, loggedInUser);
            redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã đánh giá sản phẩm!");
            return "redirect:/orders/" + orderId;
        } catch (Exception ex) {
            ex.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/reviews/order/" + orderId + "/new";
        }
    }

    @GetMapping("/order/{orderId}")
    public String viewOrderReviews(@PathVariable Integer orderId,
                                   HttpSession session,
                                   Model model) {
        Account loggedInUser = (Account) session.getAttribute("loggedInUser");
        List<ReviewView> reviews = reviewService.getOrderReviewsForCustomer(orderId, loggedInUser);
        model.addAttribute("reviews", reviews);
        model.addAttribute("orderId", orderId);
        return "reviews/order_reviews_list";
    }

    @GetMapping("/api/product/{productId}")
    @ResponseBody
    public ResponseEntity<List<ReviewView>> getProductReviews(@PathVariable Integer productId) {
        return ResponseEntity.ok(reviewService.getPublishedReviewsForProduct(productId));
    }

    // ======================== MANAGER ACTIONS ========================
    @GetMapping("/manager")
    public String manageReviews(Model model,
                                @RequestParam(value = "message", required = false) String message) {
        model.addAttribute("reviews", reviewService.getReviewsForManagement());
        if (StringUtils.hasText(message)) {
            model.addAttribute("successMessage", message);
        }
        return "reviews/manage_reviews";
    }

    @PostMapping("/manager/{reviewId}/reply")
    public String respondToReview(@PathVariable Integer reviewId,
                                  @RequestParam("reply") String reply,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        Account admin = (Account) session.getAttribute("loggedInUser");
        try {
            reviewService.respondToReview(reviewId, reply, admin);
            redirectAttributes.addFlashAttribute("successMessage", "Đã phản hồi người dùng.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/reviews/manager";
    }

    @PostMapping("/manager/{reviewId}/hide")
    public String hideReview(@PathVariable Integer reviewId, RedirectAttributes redirectAttributes) {
        if (reviewService.updateReviewStatus(reviewId, ReviewStatus.HIDDEN)) {
            redirectAttributes.addFlashAttribute("successMessage", "Đã ẩn đánh giá.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể ẩn đánh giá.");
        }
        return "redirect:/reviews/manager";
    }

    @PostMapping("/manager/{reviewId}/publish")
    public String publishReview(@PathVariable Integer reviewId, RedirectAttributes redirectAttributes) {
        if (reviewService.updateReviewStatus(reviewId, ReviewStatus.PUBLISHED)) {
            redirectAttributes.addFlashAttribute("successMessage", "Đã hiển thị lại đánh giá.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể hiển thị đánh giá.");
        }
        return "redirect:/reviews/manager";
    }

    @PostMapping("/manager/{reviewId}/spam")
    public String flagSpam(@PathVariable Integer reviewId,
                           @RequestParam(value = "flag", defaultValue = "true") boolean flag,
                           RedirectAttributes redirectAttributes) {
        if (reviewService.toggleSpamFlag(reviewId, flag)) {
            redirectAttributes.addFlashAttribute("successMessage",
                    flag ? "Đã đánh dấu đánh giá là spam." : "Đã bỏ đánh dấu spam.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật trạng thái spam.");
        }
        return "redirect:/reviews/manager";
    }

    @PostMapping("/manager/{reviewId}/delete")
    public String deleteReview(@PathVariable Integer reviewId, RedirectAttributes redirectAttributes) {
        if (reviewService.deleteReview(reviewId)) {
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa đánh giá.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa đánh giá.");
        }
        return "redirect:/reviews/manager";
    }
}

