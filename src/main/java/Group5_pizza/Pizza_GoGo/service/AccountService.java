package Group5_pizza.Pizza_GoGo.service;

import Group5_pizza.Pizza_GoGo.DTO.AccountDTO;
import Group5_pizza.Pizza_GoGo.model.Account;
import java.util.List;
import java.util.Optional;

/**
 * Interface AccountService ĐẦY ĐỦ
 * (Đã hợp nhất các hàm cho User và Admin)
 */
public interface AccountService {

    // --- Chức năng User (Login/Register/...) ---

    // ❗ SỬA LỖI: Bổ sung 2 hàm này
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    Account findByUsername(String username);
    Account findByEmail(String email);
    Account login(String username, String password);
    Account registerGoogleUser(String email, String name);
    boolean resetPassword(String username, String newPassword);
    Account save(Account account); // Dùng chung
    Account registerNewCustomer(String username, String email, String password);
    void updateAccountProfile(Account updatedAccount);

    // --- Chức năng Quản lý (Admin) ---
    List<AccountDTO> searchAccounts(String search, Integer roleId);
    AccountDTO getAccountDTOById(Integer id);
    Account getAccountById(Integer id);
    Account createAccount(AccountDTO accountDTO);
    Account updateAccount(Integer id, AccountDTO accountDTO);
    void deleteAccount(Integer id);
}