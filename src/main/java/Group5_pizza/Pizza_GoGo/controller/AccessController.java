package Group5_pizza.Pizza_GoGo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import Group5_pizza.Pizza_GoGo.model.Account;
import jakarta.servlet.http.HttpSession;

@Controller
public class AccessController {

    @GetMapping("/access-denied")
    public String accessDenied(Model model, HttpSession session) {
        Account user = (Account) session.getAttribute("loggedInUser");
        model.addAttribute("user", user);
        return "access_denied";
    }
}

