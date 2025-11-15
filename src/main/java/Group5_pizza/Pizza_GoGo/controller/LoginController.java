package Group5_pizza.Pizza_GoGo.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import Group5_pizza.Pizza_GoGo.model.Account;
import Group5_pizza.Pizza_GoGo.service.AccountService;
import Group5_pizza.Pizza_GoGo.service.GoogleOAuthService;
import Group5_pizza.Pizza_GoGo.service.TokenCacheService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final AccountService accountService;
    private final GoogleOAuthService googleOAuthService;
    private final TokenCacheService tokenCacheService;

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
                        @RequestParam(value = "rememberMe", required = false) String rememberMe,
                        HttpSession session,
                        HttpServletResponse response,
                        RedirectAttributes redirectAttributes) {
        Account account = accountService.login(username, password);
        if (account == null) {
            redirectAttributes.addAttribute("error", "true");
            return "redirect:/login";
        }

        session.setAttribute("loggedInUser", account);
        // Lưu role vào session với uppercase (ADMIN, STAFF, CUSTOMER)
        String roleName = account.getRole() != null ? account.getRole().getRoleName().toUpperCase() : "";
        session.setAttribute("loggedInUserRole", roleName);
        
        // Xử lý Remember Me
        if ("on".equals(rememberMe) || "true".equals(rememberMe)) {
            String token = UUID.randomUUID().toString();
            tokenCacheService.saveRememberMeToken(token, account.getUsername() != null ? account.getUsername() : username);
            
            // Tạo cookie với thời hạn 30 ngày
            Cookie cookie = new Cookie("rememberMe", token);
            cookie.setMaxAge(30 * 24 * 60 * 60); // 30 ngày
            cookie.setPath("/");
            cookie.setHttpOnly(true); // Bảo mật hơn, JavaScript không thể truy cập
            response.addCookie(cookie);
        }
        
        // Kiểm tra role để redirect (chỉ có 3 role: ADMIN, STAFF, CUSTOMER)
        String role = roleName;
        switch (role) {
            case "ADMIN":
            case "STAFF":
                return "redirect:/manager/dashboard";
            case "CUSTOMER":
            default:
                return "redirect:/homepage";
        }
    }

    // =================== GOOGLE OAUTH2 ===================

    @GetMapping("/login/google")
    public String redirectToGoogle() {
        return "redirect:" + googleOAuthService.getAuthorizationUrl();
    }

    @GetMapping("/login/oauth2/code/google")
    public String googleCallback(@RequestParam("code") String code, 
                                HttpSession session, 
                                jakarta.servlet.http.HttpServletResponse response,
                                RedirectAttributes redirectAttributes) {
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
            // Lưu role vào session với uppercase (ADMIN, STAFF, CUSTOMER)
            String roleName = account.getRole() != null ? account.getRole().getRoleName().toUpperCase() : "";
            session.setAttribute("loggedInUserRole", roleName);
            
            // Tự động lưu Remember Me cho đăng nhập Google
            String token = UUID.randomUUID().toString();
            String username = account.getUsername() != null ? account.getUsername() : email;
            tokenCacheService.saveRememberMeToken(token, username);
            
            // Tạo cookie với thời hạn 30 ngày
            Cookie cookie = new Cookie("rememberMe", token);
            cookie.setMaxAge(30 * 24 * 60 * 60); // 30 ngày
            cookie.setPath("/");
            cookie.setHttpOnly(true); // Bảo mật hơn, JavaScript không thể truy cập
            response.addCookie(cookie);
            
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
    public String logout(HttpSession session, jakarta.servlet.http.HttpServletRequest request, 
                         jakarta.servlet.http.HttpServletResponse response) {
        // Xóa Remember Me cookie nếu có
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("rememberMe".equals(cookie.getName()) && cookie.getValue() != null) {
                    // Xóa token khỏi Redis
                    tokenCacheService.deleteRememberMeToken(cookie.getValue());
                    
                    // Xóa cookie
                    Cookie deleteCookie = new Cookie("rememberMe", "");
                    deleteCookie.setMaxAge(0);
                    deleteCookie.setPath("/");
                    response.addCookie(deleteCookie);
                    break;
                }
            }
        }
        
        session.invalidate(); // Xóa toàn bộ session
        return "redirect:/";
    }


}