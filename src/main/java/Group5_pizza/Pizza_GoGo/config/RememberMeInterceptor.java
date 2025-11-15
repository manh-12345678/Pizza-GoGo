package Group5_pizza.Pizza_GoGo.config;

import Group5_pizza.Pizza_GoGo.model.Account;
import Group5_pizza.Pizza_GoGo.service.AccountService;
import Group5_pizza.Pizza_GoGo.service.TokenCacheService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class RememberMeInterceptor implements HandlerInterceptor {

    private final TokenCacheService tokenCacheService;
    private final AccountService accountService;

    @Override
    public boolean preHandle(@org.springframework.lang.NonNull HttpServletRequest request, 
                             @org.springframework.lang.NonNull HttpServletResponse response, 
                             @org.springframework.lang.NonNull Object handler) {
        // Chỉ xử lý nếu chưa đăng nhập
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("loggedInUser") != null) {
            return true; // Đã đăng nhập rồi, không cần xử lý
        }

        // Kiểm tra cookie Remember Me
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return true;
        }

        String rememberMeToken = null;
        for (Cookie cookie : cookies) {
            if ("rememberMe".equals(cookie.getName())) {
                rememberMeToken = cookie.getValue();
                break;
            }
        }

        if (rememberMeToken != null && !rememberMeToken.isEmpty()) {
            try {
                // Lấy username từ token
                String username = tokenCacheService.getUsernameByRememberMeToken(rememberMeToken);
                if (username != null) {
                    // Tìm account với role được load (tránh LazyInitializationException)
                    Account account = accountService.findByUsernameWithRole(username);
                    if (account != null && !Boolean.TRUE.equals(account.getIsDeleted())) {
                        // Tạo session mới nếu chưa có
                        if (session == null) {
                            session = request.getSession(true);
                        }
                        
                        // Lưu thông tin vào session
                        session.setAttribute("loggedInUser", account);
                        String roleName = account.getRole() != null ? account.getRole().getRoleName().toUpperCase() : "";
                        session.setAttribute("loggedInUserRole", roleName);
                        
                        log.info("Auto-login successful for user: {}", username);
                    } else {
                        // Account không tồn tại hoặc đã bị xóa, xóa cookie
                        Cookie deleteCookie = new Cookie("rememberMe", "");
                        deleteCookie.setMaxAge(0);
                        deleteCookie.setPath("/");
                        response.addCookie(deleteCookie);
                        tokenCacheService.deleteRememberMeToken(rememberMeToken);
                    }
                } else {
                    // Token không hợp lệ, xóa cookie
                    Cookie deleteCookie = new Cookie("rememberMe", "");
                    deleteCookie.setMaxAge(0);
                    deleteCookie.setPath("/");
                    response.addCookie(deleteCookie);
                }
            } catch (Exception e) {
                log.error("Error processing remember me token: {}", e.getMessage(), e);
            }
        }

        return true;
    }
}

