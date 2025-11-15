package Group5_pizza.Pizza_GoGo.service.impl;

import Group5_pizza.Pizza_GoGo.DTO.AccountDTO;
import Group5_pizza.Pizza_GoGo.model.Account;
import Group5_pizza.Pizza_GoGo.model.Role;
import Group5_pizza.Pizza_GoGo.repository.AccountRepository;
import Group5_pizza.Pizza_GoGo.repository.RoleRepository;
import Group5_pizza.Pizza_GoGo.service.AccountService;
import Group5_pizza.Pizza_GoGo.util.HashUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;

    // --- Chức năng User (Login/Register/...) ---

    @Override
    public boolean existsByUsername(String username) {
        return accountRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return accountRepository.existsByEmail(email);
    }

    @Override
    public Account findByUsername(String username) {
        return accountRepository.findByUsername(username).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Account findByUsernameWithRole(String username) {
        return accountRepository.findByUsernameWithRole(username).orElse(null);
    }

    @Override
    public Account findByEmail(String email) {
        return accountRepository.findByEmailAndIsDeletedFalse(email).orElse(null);
    }

    @Override
    public Account login(String username, String password) {
        String hashedInput = HashUtil.sha256ToMd5(password);

        return accountRepository.findByUsernameAndIsDeletedFalse(username)
                .filter(acc -> Boolean.TRUE.equals(acc.getIsConfirmed()))
                .filter(acc -> hashedInput.equals(acc.getPasswordHash()))
                .orElse(null);
    }

    @Override
    @Transactional
    public Account registerNewCustomer(String username, String email, String password) {
        Role customerRole = roleRepository.findByRoleName("CUSTOMER")
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Role 'CUSTOMER'. Hãy chạy DataSeeder."));

        Account account = new Account();
        account.setUsername(username);
        account.setEmail(email);
        account.setPasswordHash(HashUtil.sha256ToMd5(password));
        account.setRole(customerRole);
        account.setFullName("");
        account.setIsDeleted(false);
        account.setIsConfirmed(false); // Bắt buộc xác thực

        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public Account registerGoogleUser(String email, String name) {
        Role customerRole = roleRepository.findByRoleName("CUSTOMER")
                .orElseThrow(() -> new EntityNotFoundException("Role 'CUSTOMER' không tồn tại."));

        Account account = new Account();
        account.setEmail(email);
        account.setUsername(email);
        account.setFullName(name);
        account.setRole(customerRole);
        account.setPasswordHash(HashUtil.sha256ToMd5(UUID.randomUUID().toString()));
        account.setIsDeleted(false);
        account.setIsConfirmed(true);
        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public boolean resetPassword(String username, String newPassword) {
        Optional<Account> opt = accountRepository.findByUsername(username);
        if (opt.isEmpty())
            return false;

        Account acc = opt.get();
        acc.setPasswordHash(HashUtil.sha256ToMd5(newPassword));
        acc.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(acc);
        return true;
    }

    @Override
    public Account save(Account account) {
        // Dùng để lưu các thay đổi đơn giản (vd: confirm account)
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public void updateAccountProfile(Account updatedAccount) {
        if (updatedAccount == null || updatedAccount.getUserId() == null) {
            throw new IllegalArgumentException("Account hoặc User ID không được để trống");
        }
        Integer userId = updatedAccount.getUserId();
        Account existingAccount = accountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

        existingAccount.setFullName(updatedAccount.getFullName());
        existingAccount.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(existingAccount);
    }

    // --- Chức năng Quản lý (Admin) ---

    @Override
    @Transactional(readOnly = true)
    public List<AccountDTO> searchAccounts(String search, Integer roleId) {
        List<Account> accounts;
        boolean hasSearch = search != null && !search.trim().isEmpty();
        boolean hasRole = roleId != null;

        if (hasSearch && hasRole) {
            accounts = accountRepository.findByRoleRoleIdAndIsDeletedFalseAndUsernameContainingOrRoleRoleIdAndIsDeletedFalseAndEmailContainingOrRoleRoleIdAndIsDeletedFalseAndFullNameContaining(
                    roleId, search, roleId, search, roleId, search
            );
        } else if (hasSearch) {
            accounts = accountRepository.findByIsDeletedFalseAndUsernameContainingOrIsDeletedFalseAndEmailContainingOrIsDeletedFalseAndFullNameContaining(search, search, search);
        } else if (hasRole) {
            accounts = accountRepository.findByRoleRoleIdAndIsDeletedFalse(roleId);
        } else {
            accounts = accountRepository.findByIsDeletedFalse();
        }

        return accounts.stream()
                .map(AccountDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDTO getAccountDTOById(Integer id) {
        Account account = getAccountById(id);
        return AccountDTO.fromEntity(account);
    }

    @Override
    public Account getAccountById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Account ID không được để trống");
        }
        return accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Account ID: " + id));
    }

    @Override
    @Transactional
    public Account createAccount(AccountDTO accountDTO) {
        if (existsByUsername(accountDTO.getUsername())) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }
        if (existsByEmail(accountDTO.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        Integer roleId = accountDTO.getRoleId();
        if (roleId == null) {
            throw new IllegalArgumentException("Role ID không được để trống");
        }
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Role ID: " + roleId));

        Account account = new Account();
        account.setUsername(accountDTO.getUsername());
        account.setEmail(accountDTO.getEmail());
        account.setFullName(accountDTO.getFullName());
        account.setRole(role);

        if (accountDTO.getPassword() == null || accountDTO.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu là bắt buộc khi tạo tài khoản");
        }
        account.setPasswordHash(HashUtil.sha256ToMd5(accountDTO.getPassword()));

        account.setIsDeleted(false);
        account.setIsConfirmed(accountDTO.getIsConfirmed() != null ? accountDTO.getIsConfirmed() : true);

        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public Account updateAccount(Integer id, AccountDTO accountDTO) {
        Account account = getAccountById(id);

        Optional<Account> existingEmail = accountRepository.findByEmail(accountDTO.getEmail());
        if (existingEmail.isPresent() && !existingEmail.get().getUserId().equals(id)) {
            throw new IllegalArgumentException("Email đã được sử dụng bởi tài khoản khác");
        }

        Optional<Account> existingUsername = accountRepository.findByUsername(accountDTO.getUsername());
        if (existingUsername.isPresent() && !existingUsername.get().getUserId().equals(id)) {
            throw new IllegalArgumentException("Username đã được sử dụng bởi tài khoản khác");
        }

        Integer roleId = accountDTO.getRoleId();
        if (roleId == null) {
            throw new IllegalArgumentException("Role ID không được để trống");
        }
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Role ID: " + roleId));

        account.setUsername(accountDTO.getUsername());
        account.setEmail(accountDTO.getEmail());
        account.setFullName(accountDTO.getFullName());
        account.setRole(role);
        account.setIsConfirmed(accountDTO.getIsConfirmed());
        account.setIsDeleted(accountDTO.getIsDeleted());
        account.setUpdatedAt(LocalDateTime.now());

        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public void deleteAccount(Integer id) {
        Account account = getAccountById(id);
        account.setIsDeleted(true);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
    }
}