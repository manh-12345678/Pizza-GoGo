package Group5_pizza.Pizza_GoGo.controller;

import Group5_pizza.Pizza_GoGo.DTO.AccountDTO;
import Group5_pizza.Pizza_GoGo.model.Role;
import Group5_pizza.Pizza_GoGo.repository.RoleRepository;
import Group5_pizza.Pizza_GoGo.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller Quản lý Tài khoản (Admin)
 * Đồng nhất layout với /combos, /products...
 */
@Controller
@RequestMapping({"/accounts","/manager/accounts"})
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final RoleRepository roleRepository; // Dùng để lấy danh sách roles cho dropdown

    /**
     * Hiển thị trang quản lý danh sách tài khoản
     * URL: /accounts/manage
     */
    @GetMapping("/manage")
    public String listAccounts(@RequestParam(required = false) String search,
                               @RequestParam(required = false) Integer roleId,
                               Model model) {
        try {
            List<AccountDTO> accounts = accountService.searchAccounts(search, roleId);
            List<Role> roles = roleRepository.findAll();

            model.addAttribute("accounts", accounts);
            model.addAttribute("roles", roles);
            model.addAttribute("search", search);
            model.addAttribute("roleId", roleId);

            return "accounts/manage_accounts"; // Trả về file Thymeleaf
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi tải danh sách tài khoản: " + e.getMessage());
            return "accounts/manage_accounts";
        }
    }

    /**
     * Hiển thị form để THÊM MỚI tài khoản
     * URL: /accounts/add
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        List<Role> roles = roleRepository.findAll();

        model.addAttribute("accountDTO", new AccountDTO());
        model.addAttribute("allRoles", roles);
        model.addAttribute("isEdit", false);

        return "accounts/account_form";
    }

    /**
     * Xử lý THÊM MỚI tài khoản
     * URL: /accounts/add (POST)
     */
    @PostMapping("/add")
    public String addAccount(@ModelAttribute("accountDTO") AccountDTO accountDTO,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        try {
            accountService.createAccount(accountDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo tài khoản thành công!");
            return "redirect:/manager/accounts/manage";
        } catch (Exception e) {
            // Nếu lỗi, trả về form
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            model.addAttribute("accountDTO", accountDTO);
            model.addAttribute("allRoles", roleRepository.findAll());
            model.addAttribute("isEdit", false);
            return "accounts/account_form";
        }
    }

    /**
     * Hiển thị form để CHỈNH SỬA tài khoản
     * URL: /accounts/edit/{id}
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            AccountDTO accountDTO = accountService.getAccountDTOById(id);
            List<Role> roles = roleRepository.findAll();

            model.addAttribute("accountDTO", accountDTO);
            model.addAttribute("allRoles", roles);
            model.addAttribute("isEdit", true);

            return "accounts/account_form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy tài khoản: " + e.getMessage());
            return "redirect:/manager/accounts/manage";
        }
    }

    /**
     * Xử lý CẬP NHẬT tài khoản
     * URL: /accounts/edit/{id} (POST)
     */
    @PostMapping("/edit/{id}")
    public String updateAccount(@PathVariable Integer id,
                                @ModelAttribute("accountDTO") AccountDTO accountDTO,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        try {
            accountService.updateAccount(id, accountDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tài khoản thành công!");
            return "redirect:/manager/accounts/manage";
        } catch (Exception e) {
            // Nếu lỗi, trả về form
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            model.addAttribute("accountDTO", accountDTO);
            model.addAttribute("allRoles", roleRepository.findAll());
            model.addAttribute("isEdit", true);
            return "accounts/account_form";
        }
    }

    /**
     * Xử lý XÓA (mềm) tài khoản
     * URL: /accounts/delete/{id} (POST)
     */
    @PostMapping("/delete/{id}")
    public String deleteAccount(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            accountService.deleteAccount(id);
            redirectAttributes.addFlashAttribute("successMessage", "Vô hiệu hóa tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa tài khoản: " + e.getMessage());
        }
        return "redirect:/manager/accounts/manage";
    }
}