package Group5_pizza.Pizza_GoGo.controller;

import Group5_pizza.Pizza_GoGo.model.Account;
import Group5_pizza.Pizza_GoGo.model.Payment;
import Group5_pizza.Pizza_GoGo.service.PaymentEntityService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/manager/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentManagementController {

    private final PaymentEntityService paymentEntityService;

    // Trang quản lý thanh toán
    @GetMapping
    public String showPaymentManagement(
            @RequestParam(required = false) Integer orderId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentMethod,
            HttpSession session,
            Model model) {
        
        Account loggedInUser = (Account) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        List<Payment> payments;
        
        if (orderId != null) {
            payments = paymentEntityService.getPaymentsByOrderId(orderId);
        } else if (status != null && !status.isEmpty()) {
            payments = paymentEntityService.getAllPayments().stream()
                    .filter(p -> status.equals(p.getStatus()))
                    .toList();
        } else if (paymentMethod != null && !paymentMethod.isEmpty()) {
            payments = paymentEntityService.getAllPayments().stream()
                    .filter(p -> paymentMethod.equals(p.getPaymentMethod()))
                    .toList();
        } else {
            payments = paymentEntityService.getAllPayments();
        }

        model.addAttribute("payments", payments);
        model.addAttribute("selectedOrderId", orderId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedPaymentMethod", paymentMethod);
        return "manager/payments";
    }

    // Xác nhận thanh toán COD
    @PostMapping("/{paymentId}/confirm")
    public String confirmCODPayment(
            @PathVariable Integer paymentId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            Account loggedInUser = (Account) session.getAttribute("loggedInUser");
            if (loggedInUser == null) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập");
                return "redirect:/login";
            }

            paymentEntityService.confirmCODPayment(paymentId, loggedInUser.getUserId());
            redirectAttributes.addFlashAttribute("message", "Đã xác nhận thanh toán COD thành công");
            return "redirect:/manager/payments";

        } catch (Exception e) {
            log.error("Lỗi khi xác nhận thanh toán COD: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/manager/payments";
        }
    }

    // Hủy thanh toán COD
    @PostMapping("/{paymentId}/cancel")
    public String cancelCODPayment(
            @PathVariable Integer paymentId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            Account loggedInUser = (Account) session.getAttribute("loggedInUser");
            if (loggedInUser == null) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập");
                return "redirect:/login";
            }

            paymentEntityService.cancelCODPayment(paymentId);
            redirectAttributes.addFlashAttribute("message", "Đã hủy thanh toán thành công");
            return "redirect:/manager/payments";

        } catch (Exception e) {
            log.error("Lỗi khi hủy thanh toán: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/manager/payments";
        }
    }

    // Xem chi tiết thanh toán
    @GetMapping("/{paymentId}")
    public String viewPaymentDetail(
            @PathVariable Integer paymentId,
            HttpSession session,
            Model model) {
        try {
            Account loggedInUser = (Account) session.getAttribute("loggedInUser");
            if (loggedInUser == null) {
                return "redirect:/login";
            }

            Payment payment = paymentEntityService.getPaymentById(paymentId);
            model.addAttribute("payment", payment);
            return "manager/payment_detail";

        } catch (Exception e) {
            log.error("Lỗi khi xem chi tiết thanh toán: {}", e.getMessage(), e);
            return "redirect:/manager/payments";
        }
    }
}

