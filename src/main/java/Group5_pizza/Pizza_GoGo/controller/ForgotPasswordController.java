package Group5_pizza.Pizza_GoGo.controller;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import Group5_pizza.Pizza_GoGo.model.Account;
import Group5_pizza.Pizza_GoGo.service.AccountService;
import Group5_pizza.Pizza_GoGo.service.MailService;
import Group5_pizza.Pizza_GoGo.service.TokenCacheService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final AccountService accountService;
    private final MailService emailService;
    private final TokenCacheService tokenCacheService; // Dùng Redis cache

    // Hiển thị form quên mật khẩu
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot/forgot-password"; // nằm trong folder forgot/
    }

    // Xử lý gửi mail reset password
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
                                        RedirectAttributes redirectAttributes) {
        Account account = accountService.findByEmail(email);
        if (account == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy tài khoản với email này!");
            return "redirect:/forgot-password";
        }

        // Tạo token và lưu vào Redis
        String token = UUID.randomUUID().toString();
        tokenCacheService.saveToken(token, account.getUsername());

        String resetLink = "http://localhost:8080/reset-password?token=" + token;
        String subject = "Đặt lại mật khẩu Pizza GoGo 🍕";
        String body = "<p>Xin chào,</p>"
                + "<p>Bạn vừa yêu cầu đặt lại mật khẩu. Nhấn vào link bên dưới để tiếp tục:</p>"
                + "<p><a href=\"" + resetLink + "\">Đặt lại mật khẩu</a></p>"
                + "<p>Nếu bạn không yêu cầu, vui lòng bỏ qua email này.</p>";

        emailService.sendMail(email, subject, body);

        redirectAttributes.addFlashAttribute("success", "Link đặt lại mật khẩu đã được gửi qua email!");
        return "redirect:/forgot-password";
    }

    // Hiển thị form nhập mật khẩu mới
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        String username = tokenCacheService.getUsernameByToken(token);
        if (username == null) {
            model.addAttribute("error", "Token không hợp lệ hoặc đã hết hạn!");
            return "forgot/reset-password"; // view trong folder forgot/
        }

        model.addAttribute("token", token);
        return "forgot/reset-password";
    }

    // Xử lý đặt lại mật khẩu mới
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String newPassword,
                                       RedirectAttributes redirectAttributes) {

        String username = tokenCacheService.getUsernameByToken(token);
        if (username == null) {
            redirectAttributes.addFlashAttribute("error", "Token không hợp lệ hoặc đã hết hạn!");
            return "redirect:/reset-password?token=" + token;
        }

        boolean result = accountService.resetPassword(username, newPassword);
        if (result) {
            tokenCacheService.deleteToken(token); // Xóa token sau khi dùng
            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công! Hãy đăng nhập lại.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi đổi mật khẩu. Vui lòng thử lại!");
            return "redirect:/reset-password?token=" + token;
        }
    }
}