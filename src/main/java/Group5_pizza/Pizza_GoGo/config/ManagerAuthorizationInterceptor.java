package Group5_pizza.Pizza_GoGo.config;

import java.io.IOException;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import Group5_pizza.Pizza_GoGo.model.Account;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class ManagerAuthorizationInterceptor implements HandlerInterceptor {

    // Chỉ có 3 role: ADMIN, STAFF, CUSTOMER
    private static final Set<String> ALLOWED_ROLES = Set.of("admin", "staff");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        HttpSession session = request.getSession(false);
        Account loggedInUser = session != null ? (Account) session.getAttribute("loggedInUser") : null;
        String roleName = session != null ? (String) session.getAttribute("loggedInUserRole") : null;

        if (roleName == null && loggedInUser != null && loggedInUser.getRole() != null) {
            roleName = loggedInUser.getRole().getRoleName();
        }

        if (loggedInUser == null) {
            response.sendRedirect("/login");
            return false;
        }

        // Chuyển về lowercase để so sánh
        String roleLower = roleName != null ? roleName.toLowerCase() : null;
        boolean authorized = roleLower != null && ALLOWED_ROLES.contains(roleLower);

        if (!authorized) {
            response.sendRedirect("/access-denied");
            return false;
        }

        return true;
    }
}

