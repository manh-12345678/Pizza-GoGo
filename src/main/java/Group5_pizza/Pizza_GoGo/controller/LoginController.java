package Group5_pizza.Pizza_GoGo.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import Group5_pizza.Pizza_GoGo.model.Account;
import Group5_pizza.Pizza_GoGo.service.AccountService;
import Group5_pizza.Pizza_GoGo.service.GoogleOAuthService;
import Group5_pizza.Pizza_GoGo.service.MailService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final AccountService accountService;
    private final GoogleOAuthService googleOAuthService;
    private final MailService mailService;

    // =================== LOGIN ===================

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "resetSuccess", required = false) String resetSuccess,
                                Model model) {
        if (error != null) {
            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng.");
        }
        if (resetSuccess != null) {
            model.addAttribute("message", "Mật khẩu đã được thay đổi thành công! Vui lòng đăng nhập.");
        }
        return "Login/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        Account account = accountService.login(username, password);
        if (account == null) {
            redirectAttributes.addAttribute("error", "true");
            return "redirect:/login";
        }

        session.setAttribute("loggedInUser", account);
        String role = account.getRole().getRoleName();
        switch (role.toUpperCase()) {
            case "ADMIN":
                return "redirect:/admin/dashboard";
            case "STAFF":
                return "redirect:/staff/dashboard";
            default: // CUSTOMER
                return "redirect:/homepage";
        }
    }

    // =================== GOOGLE OAUTH2 ===================

    @GetMapping("/login/google")
    public String redirectToGoogle() {
        return "redirect:" + googleOAuthService.getAuthorizationUrl();
    }

    @GetMapping("/login/oauth2/code/google")
    public String googleCallback(@RequestParam("code") String code, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            String accessToken = googleOAuthService.getAccessToken(code);
            Map<String, Object> userInfo = googleOAuthService.getUserInfo(accessToken);

            String email = (String) userInfo.get("email");
            String name = (String) userInfo.get("name");

            if (email == null) {
                redirectAttributes.addAttribute("error", "Không thể lấy thông tin email từ Google.");
                return "redirect:/login";
            }

            Account account = accountService.findByUsername(email);
            if (account == null) {
                // Nếu tài khoản không tồn tại, tự động đăng ký
                account = accountService.registerGoogleUser(email, name);
            }

            // Lưu thông tin vào session và chuyển hướng
            session.setAttribute("loggedInUser", account);
            return "redirect:/homepage";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addAttribute("error", "Đăng nhập với Google thất bại.");
            return "redirect:/login";
        }
    }

    // // =================== FORGOT PASSWORD ===================

    // @GetMapping("/forgot-password")
    // public String showForgotPasswordPage(@RequestParam(value = "error", required = false) String error,
    //                                      @RequestParam(value = "message", required = false) String message,
    //                                      Model model) {
    //     if (error != null) {
    //         model.addAttribute("error", error);
    //     }
    //     if (message != null) {
    //         model.addAttribute("message", message);
    //     }
    //     return "forgot-password";
    // }

    // @PostMapping("/forgot-password")
    // public String handleForgotPassword(@RequestParam String email, RedirectAttributes redirectAttributes) {
    //     Account account = accountService.findByUsername(email);
    //     if (account == null) {
    //         redirectAttributes.addAttribute("error", "Không tìm thấy tài khoản nào với email này.");
    //         return "redirect:/forgot-password";
    //     }

    //     String token = UUID.randomUUID().toString();
    //     accountService.createPasswordResetToken(account, token);

    //     String resetLink = "http://localhost:8080/reset-password?token=" + token;
    //     mailService.sendResetLink(email, resetLink); // Sử dụng hàm sendResetLink cho nhất quán

    //     redirectAttributes.addAttribute("message", "Link đặt lại mật khẩu đã được gửi tới email của bạn.");
    //     return "redirect:/forgot-password";
    // }

    // @GetMapping("/reset-password")
    // public String showResetPasswordForm(@RequestParam("token") String token,
    //                                     Model model,
    //                                     @RequestParam(value = "error", required = false) String error) {
    //     Account account = accountService.validatePasswordResetToken(token);
    //     if (account == null) {
    //         model.addAttribute("error", "Token không hợp lệ hoặc đã hết hạn.");
    //         return "error-page"; // Nên có một trang lỗi chung
    //     }
    //     if (error != null) {
    //         model.addAttribute("error", error);
    //     }
    //     model.addAttribute("token", token);
    //     return "reset-password";
    // }

    // @PostMapping("/reset-password")
    // public String handleResetPassword(@RequestParam("token") String token,
    //                                   @RequestParam("newPassword") String newPassword,
    //                                   RedirectAttributes redirectAttributes) {
    //     boolean success = accountService.resetPassword(token, newPassword);
    //     if (!success) {
    //         redirectAttributes.addAttribute("token", token);
    //         redirectAttributes.addAttribute("error", "Token không hợp lệ hoặc đã hết hạn. Vui lòng thử lại.");
    //         return "redirect:/reset-password";
    //     }
    //     return "redirect:/login?resetSuccess=true";
    // }

    // =================== LOGOUT ===================

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Xóa toàn bộ session
        return "redirect:/";
    }


}