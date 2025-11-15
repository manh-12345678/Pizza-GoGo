package Group5_pizza.Pizza_GoGo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import Group5_pizza.Pizza_GoGo.model.Account;
import Group5_pizza.Pizza_GoGo.service.AccountService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

    private final AccountService accountService;

    @GetMapping
    public String showProfile(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Account loggedInUser = (Account) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để xem trang cá nhân.");
            return "redirect:/login";
    
        }

        model.addAttribute("account", loggedInUser);
        return "profile/profile"; // file profile.html trong thư mục templates/reProfile/
    }

   @PostMapping("/update")
public String updateProfile(@ModelAttribute("account") Account updatedAccount,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
    Account loggedInUser = (Account) session.getAttribute("loggedInUser");

    if (loggedInUser == null) {
        redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập để cập nhật thông tin.");
        return "redirect:/login";
    }

    updatedAccount.setUserId(loggedInUser.getUserId());
    updatedAccount.setUsername(loggedInUser.getUsername());
    updatedAccount.setRole(loggedInUser.getRole());

    accountService.updateAccountProfile(updatedAccount);
    session.setAttribute("loggedInUser", updatedAccount);

    redirectAttributes.addFlashAttribute("message", "Cập nhật thông tin thành công!");
    return "redirect:/profile";
}

   @PostMapping("/change-password")
public String changePassword(@RequestParam("currentPassword") String currentPassword,
                             @RequestParam("newPassword") String newPassword,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
    Account loggedInUser = (Account) session.getAttribute("loggedInUser");
    if (loggedInUser == null) {
        redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập để đổi mật khẩu.");
        return "redirect:/login";
    }

    // Verify current password by attempting login
    Account verified = accountService.login(loggedInUser.getUsername(), currentPassword);
    if (verified == null) {
        redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng.");
        return "redirect:/profile";
    }

    // Reset to new password
    boolean success = accountService.resetPassword(loggedInUser.getUsername(), newPassword);
    if (!success) {
        redirectAttributes.addFlashAttribute("error", "Không thể đổi mật khẩu.");
        return "redirect:/profile";
    }

    redirectAttributes.addFlashAttribute("message", "Đổi mật khẩu thành công!");
    return "redirect:/profile";
}

}
