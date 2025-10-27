package Group5_pizza.Pizza_GoGo.controller;

import Group5_pizza.Pizza_GoGo.model.Account;
import Group5_pizza.Pizza_GoGo.service.AccountService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ChangePasswordController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/change-password")
    public String showChangePasswordForm() {
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmNewPassword") String confirmNewPassword,
                                 HttpSession session,
                                 Model model) {
        Account user = (Account) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            model.addAttribute("error", "Current password is required");
            return "change-password";
        }
        if (newPassword == null || newPassword.length() < 6) {
            model.addAttribute("error", "New password must be at least 6 characters");
            return "change-password";
        }
        if (!newPassword.equals(confirmNewPassword)) {
            model.addAttribute("error", "New passwords do not match");
            return "change-password";
        }
        if (!user.getPasswordHash().equals(currentPassword)) {
            model.addAttribute("error", "Current password is incorrect");
            return "change-password";
        }
        user.setPasswordHash(newPassword);
        accountService.register(user);
        model.addAttribute("success", "Password changed successfully");
        return "change-password";
    }
}
