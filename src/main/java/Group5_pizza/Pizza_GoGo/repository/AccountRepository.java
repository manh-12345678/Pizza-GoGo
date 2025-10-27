package Group5_pizza.Pizza_GoGo.repository;

import Group5_pizza.Pizza_GoGo.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);  // Thêm để check email tồn tại
    Optional<Account> findByEmail(String email);  // Thêm nếu cần
}