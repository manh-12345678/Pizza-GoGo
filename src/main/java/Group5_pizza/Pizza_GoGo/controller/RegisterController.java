package Group5_pizza.Pizza_GoGo.controller;

import Group5_pizza.Pizza_GoGo.model.Account;
import Group5_pizza.Pizza_GoGo.model.Role;
import Group5_pizza.Pizza_GoGo.service.AccountService;
import Group5_pizza.Pizza_GoGo.service.MailService;
import Group5_pizza.Pizza_GoGo.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.regex.Pattern;

@Controller
public class RegisterController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private MailService mailService;

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping("/register")
    public String showRegisterPage() {
        return "Register/register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam("fullName") String fullName,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model
    ) {
        try {
            // Input Validation
            if (fullName == null || fullName.trim().isEmpty()) {
                model.addAttribute("error", "Full name is required!");
                return "Register/register";
            }
            if (username == null || username.trim().isEmpty()) {
                model.addAttribute("error", "Username is required!");
                return "Register/register";
            }
            if (email == null || email.trim().isEmpty()) {
                model.addAttribute("error", "Email is required!");
                return "Register/register";
            }
            // Simple email validation
            Pattern emailPattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
            if (!emailPattern.matcher(email).matches()) {
                model.addAttribute("error", "Invalid email format!");
                return "Register/register";
            }
            if (password == null || password.length() < 6) {
                model.addAttribute("error", "Password must be at least 6 characters!");
                return "Register/register";
            }
            if (!password.equals(confirmPassword)) {
                model.addAttribute("error", "Passwords do not match!");
                return "Register/register";
            }

            // Business Logic Validation
            if (accountService.existsByUsername(username)) {
                model.addAttribute("error", "Username is already taken!");
                return "Register/register";
            }
            if (accountService.existsByEmail(email)) {
                model.addAttribute("error", "Email is already registered!");
                return "Register/register";
            }

            // Account Creation
            Account account = new Account();
            account.setFullName(fullName);
            account.setUsername(username);
            account.setEmail(email);
            account.setPasswordHash(password); // Service will hash this later

            // Assign "CUSTOMER" role instead of "USER"
            Role customerRole = roleRepository.findByRoleName("CUSTOMER");
            if (customerRole == null) {
                // This is a safeguard in case the DataSeeder hasn't run
                model.addAttribute("error", "System error: Default role not found. Please contact support.");
                return "Register/register";
            }
            account.setRole(customerRole);

            accountService.register(account);

            // Send Confirmation Email
            String token = UUID.randomUUID().toString();
            accountService.createConfirmationToken(account, token);
            String confirmUrl = "http://localhost:8080/confirm?token=" + token;
            mailService.sendConfirmationLink(email, confirmUrl);

            model.addAttribute("success", "Registration successful! Please check your email to confirm your account.");
            // It's better to show a confirmation message on the login page
            return "redirect:/login?registered=true";

        } catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred during registration: " + e.getMessage());
            return "Register/register";
        }
    }

    @GetMapping("/confirm")
    public String confirmAccount(@RequestParam("token") String token, Model model) {
        boolean isConfirmed = accountService.confirmAccount(token);
        if (isConfirmed) {
            // Redirect to login page with a success message
            return "redirect:/login?confirmed=true";
        } else {
            // You might want a specific error page for this
            model.addAttribute("error", "Invalid or expired confirmation link!");
            return "Register/register";
        }
    }
}