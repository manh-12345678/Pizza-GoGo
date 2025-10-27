package Group5_pizza.Pizza_GoGo.service;

import Group5_pizza.Pizza_GoGo.model.Account;

public interface AccountService {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);  // Thêm
    void register(Account account);
    Account findByEmail(String email);
    Account findByUsername(String username);
    Account login(String username, String password);
    Account registerGoogleUser(String email, String name);
    void createConfirmationToken(Account account, String token);  // Thêm cho confirm email
    Account validateConfirmationToken(String token);  // Thêm
    boolean confirmAccount(String token);  // Thêm
    void createPasswordResetToken(Account account, String token);
    Account validatePasswordResetToken(String token);
    boolean resetPassword(String name, String newPassword);
    boolean changePassword(String username, String currentPassword, String newPassword);
    void updateAccount(Account updatedAccount);
}