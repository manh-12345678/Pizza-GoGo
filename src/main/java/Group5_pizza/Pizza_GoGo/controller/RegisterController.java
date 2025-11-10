package Group5_pizza.Pizza_GoGo.controller;

import Group5_pizza.Pizza_GoGo.model.Account;
import Group5_pizza.Pizza_GoGo.service.AccountService;
import Group5_pizza.Pizza_GoGo.service.MailService;
import Group5_pizza.Pizza_GoGo.service.TokenCacheService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class RegisterController {

    private final AccountService accountService;
    private final MailService mailService;
    private final TokenCacheService tokenCacheService;

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "Register/register";
    }

    @PostMapping("/register")
    public String processRegistration(@RequestParam String username,
                                      @RequestParam String email,
                                      @RequestParam String password,
                                      @RequestParam String confirmPassword,
                                      HttpServletRequest request,
                                      RedirectAttributes redirectAttributes) {

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu không khớp!");
            return "redirect:/register";
        }
        if (accountService.existsByUsername(username)) {
            redirectAttributes.addFlashAttribute("error", "Username đã tồn tại!");
            return "redirect:/register";
        }
        if (accountService.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("error", "Email đã tồn tại!");
            return "redirect:/register";
        }

        try {
            // ✅ Không lưu DB ngay — chỉ lưu tạm thông tin đăng ký
            String token = UUID.randomUUID().toString();

            Map<String, String> pendingData = new HashMap<>();
            pendingData.put("username", username);
            pendingData.put("email", email);
            pendingData.put("password", password);

            tokenCacheService.savePendingAccount(token, pendingData);

            // Gửi email xác nhận
            String confirmationUrl = request.getRequestURL().toString().replace(request.getServletPath(), "") + "/confirm-account?token=" + token;
            mailService.sendConfirmationLink(email, confirmationUrl);

            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.");
            return "redirect:/login";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/confirm-account")
    public String confirmAccount(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        // ✅ Lấy thông tin đăng ký tạm từ Redis
        Map<String, String> pendingData = tokenCacheService.getPendingAccount(token);
        if (pendingData == null) {
            redirectAttributes.addFlashAttribute("error", "Token không hợp lệ hoặc đã hết hạn!");
            return "redirect:/login";
        }

        try {
            String username = pendingData.get("username");
            String email = pendingData.get("email");
            String password = pendingData.get("password");

            // Tạo tài khoản thật trong DB (hash password tự động trong service)
            Account account = accountService.registerNewCustomer(username, email, password);
            account.setIsConfirmed(true);
            accountService.save(account);

            // Xóa thông tin tạm
            tokenCacheService.deletePendingAccount(token);

            redirectAttributes.addFlashAttribute("success", "Xác thực tài khoản thành công! Bạn có thể đăng nhập.");
            return "redirect:/login";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Xác thực thất bại: " + e.getMessage());
            return "redirect:/login";
        }
    }
}