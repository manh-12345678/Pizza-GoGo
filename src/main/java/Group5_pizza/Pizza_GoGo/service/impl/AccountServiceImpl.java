
package Group5_pizza.Pizza_GoGo.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import Group5_pizza.Pizza_GoGo.model.Account;
import Group5_pizza.Pizza_GoGo.model.Role;
import Group5_pizza.Pizza_GoGo.repository.AccountRepository;
import Group5_pizza.Pizza_GoGo.repository.RoleRepository;
import Group5_pizza.Pizza_GoGo.service.AccountService;
import Group5_pizza.Pizza_GoGo.service.TokenCacheService;
import Group5_pizza.Pizza_GoGo.util.HashUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final TokenCacheService tokenCacheService;
    private final RoleRepository roleRepository;

    @Override
    public boolean existsByUsername(String username) {
        return accountRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return accountRepository.existsByEmail(email);
    }

    @Override
    public void register(Account account) {
        account.setPasswordHash(HashUtil.sha256ToMd5(account.getPasswordHash()));
        account.setIsDeleted(false);
        account.setIsConfirmed(false); // Chưa xác nhận
        account.setCreatedAt(LocalDateTime.now());
        accountRepository.save(account);
    }

    @Override
    public Account findByUsername(String username) {
        return accountRepository.findByUsername(username).orElse(null);
    }

    @Override
    public Account login(String username, String password) {
        String hashedInput = HashUtil.sha256ToMd5(password);
        return accountRepository.findByUsername(username)
                .filter(acc -> Boolean.FALSE.equals(acc.getIsDeleted()))
                .filter(acc -> Boolean.TRUE.equals(acc.getIsConfirmed())) // Chỉ login nếu đã confirm
                .filter(acc -> hashedInput.equals(acc.getPasswordHash()))
                .orElse(null);
    }

    @Override
    public Account registerGoogleUser(String email, String name) {
        Account account = new Account();
        account.setEmail(email);
        account.setFullName(name);
        account.setUsername(name);

        Role customerRole = roleRepository.findByRoleName("CUSTOMER");
        account.setRole(customerRole);

        account.setPasswordHash(HashUtil.sha256ToMd5(UUID.randomUUID().toString()));

        account.setIsDeleted(false);
        account.setIsConfirmed(true);
        account.setCreatedAt(LocalDateTime.now());
        return accountRepository.save(account);
    }

    @Override
    public void createConfirmationToken(Account account, String token) {
        tokenCacheService.saveToken(token, account.getUsername()); // Lưu token 10 phút
    }

    @Override
    public Account validateConfirmationToken(String token) {
        String username = tokenCacheService.getUsernameByToken(token);
        return username != null ? accountRepository.findByUsername(username).orElse(null) : null;
    }

    @Override
    public boolean confirmAccount(String token) {
        Account account = validateConfirmationToken(token);
        if (account == null)
            return false;

        account.setIsConfirmed(true);
        accountRepository.save(account);
        tokenCacheService.deleteToken(token);
        return true;
    }

    @Override
    public void createPasswordResetToken(Account account, String token) {
        tokenCacheService.saveToken(token, account.getUsername());
    }

    @Override
    public Account validatePasswordResetToken(String token) {
        String username = tokenCacheService.getUsernameByToken(token);
        return username != null ? accountRepository.findByUsername(username).orElse(null) : null;
    }
    @Override
    public Account findByEmail(String email) {
        return accountRepository.findByEmail(email).orElse(null);
    }
    @Override
    public boolean resetPassword(String username, String newPassword) {
        Optional<Account> opt = accountRepository.findByUsername(username);
        if (opt.isEmpty())
            return false;

        Account acc = opt.get();
        acc.setPasswordHash(HashUtil.sha256ToMd5(newPassword));
        accountRepository.save(acc);
        return true;
    }

    @Override
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        Account account = accountRepository.findByUsername(username)
                .orElse(null);
        if (account == null) {
            return false;
        }
        String hashedCurrent = HashUtil.sha256ToMd5(currentPassword);
        if (!hashedCurrent.equals(account.getPasswordHash())) {
            return false; // Mật khẩu cũ sai
        }
        String hashedNew = HashUtil.sha256ToMd5(newPassword);
        account.setPasswordHash(hashedNew);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
        return true;
    }

    @Override
    public void updateAccount(Account updatedAccount) {
        // Lấy tài khoản hiện tại từ DB
        Account existingAccount = accountRepository.findById(updatedAccount.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản để cập nhật"));

        existingAccount.setFullName(updatedAccount.getFullName());
        existingAccount.setUsername(existingAccount.getUsername());
        existingAccount.setEmail(existingAccount.getEmail());
        existingAccount.setRole(existingAccount.getRole());
        existingAccount.setPasswordHash(existingAccount.getPasswordHash());
        existingAccount.setIsDeleted(existingAccount.getIsDeleted());
        existingAccount.setIsConfirmed(existingAccount.getIsConfirmed());

        existingAccount.setUpdatedAt(LocalDateTime.now());

        accountRepository.save(existingAccount);
    }

}