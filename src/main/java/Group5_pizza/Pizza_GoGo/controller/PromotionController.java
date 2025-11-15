package Group5_pizza.Pizza_GoGo.controller;

import Group5_pizza.Pizza_GoGo.model.Account;
import Group5_pizza.Pizza_GoGo.model.Customer;
import Group5_pizza.Pizza_GoGo.model.Promotion;
import Group5_pizza.Pizza_GoGo.model.PromotionRedemption;
import Group5_pizza.Pizza_GoGo.service.PromotionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/promotions")
@RequiredArgsConstructor
@Slf4j
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping
    public String viewPromotions(Model model, HttpSession session) {
        List<Promotion> availablePromotions = promotionService.getAvailablePromotions();
        model.addAttribute("promotions", availablePromotions);

        // Lấy điểm của khách hàng
        Account loggedInUser = (Account) session.getAttribute("loggedInUser");
        if (loggedInUser != null && loggedInUser.getCustomer() != null) {
            Customer customer = loggedInUser.getCustomer();
            Integer points = customer.getPoints() != null ? customer.getPoints() : 0;
            model.addAttribute("customerPoints", points);
            model.addAttribute("customerId", customer.getCustomerId());
        } else {
            model.addAttribute("customerPoints", 0);
            model.addAttribute("customerId", null);
        }

        return "promotions/list";
    }

    @PostMapping("/redeem")
    public String redeemPromotion(
            @RequestParam("promotionId") Integer promotionId,
            @RequestParam(value = "notes", required = false) String notes,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        Account loggedInUser = (Account) session.getAttribute("loggedInUser");
        if (loggedInUser == null || loggedInUser.getCustomer() == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để đổi promotion");
            return "redirect:/login";
        }

        try {
            Customer customer = loggedInUser.getCustomer();
            PromotionRedemption redemption = promotionService.redeemPromotion(
                    customer.getCustomerId(), promotionId, notes);
            
            if ("COMPLETED".equals(redemption.getStatus())) {
                redirectAttributes.addFlashAttribute("success", 
                    "Đổi promotion thành công! " + 
                    (redemption.getVoucherCode() != null ? "Mã voucher: " + redemption.getVoucherCode() : ""));
            } else {
                redirectAttributes.addFlashAttribute("success", 
                    "Yêu cầu đổi promotion đã được gửi. Vui lòng chờ xác nhận từ nhân viên.");
            }
        } catch (Exception e) {
            log.error("Error redeeming promotion: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/promotions";
    }

    @GetMapping("/my-redemptions")
    public String viewMyRedemptions(Model model, HttpSession session) {
        Account loggedInUser = (Account) session.getAttribute("loggedInUser");
        if (loggedInUser == null || loggedInUser.getCustomer() == null) {
            return "redirect:/login";
        }

        Customer customer = loggedInUser.getCustomer();
        List<PromotionRedemption> redemptions = promotionService.getCustomerRedemptions(customer.getCustomerId());
        model.addAttribute("redemptions", redemptions);
        Integer points = customer.getPoints() != null ? customer.getPoints() : 0;
        model.addAttribute("customerPoints", points);

        return "promotions/my_redemptions";
    }
}

