package Group5_pizza.Pizza_GoGo.repository;

import Group5_pizza.Pizza_GoGo.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    // --- Dùng cho Login/Register/Forgot Password ---
    Optional<Account> findByUsername(String username);
    Optional<Account> findByEmail(String email);
    Optional<Account> findByEmailAndIsDeletedFalse(String email);
    Optional<Account> findByUsernameAndIsDeletedFalse(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // --- Dùng cho Admin Management ---
    List<Account> findAll();

    // Tìm theo Username HOẶC Email HOẶC FullName (chỉ tìm các account chưa xóa)
    List<Account> findByIsDeletedFalseAndUsernameContainingOrIsDeletedFalseAndEmailContainingOrIsDeletedFalseAndFullNameContaining(String username, String email, String fullName);

    // Tìm theo Role (chỉ tìm các account chưa xóa)
    List<Account> findByRoleRoleIdAndIsDeletedFalse(Integer roleId);

    // Tìm kết hợp cả 3
    List<Account> findByRoleRoleIdAndIsDeletedFalseAndUsernameContainingOrRoleRoleIdAndIsDeletedFalseAndEmailContainingOrRoleRoleIdAndIsDeletedFalseAndFullNameContaining(
            Integer roleId, String username, Integer roleId2, String email, Integer roleId3, String fullName
    );
}