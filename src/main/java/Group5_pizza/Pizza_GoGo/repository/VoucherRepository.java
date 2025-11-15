package Group5_pizza.Pizza_GoGo.repository;

import Group5_pizza.Pizza_GoGo.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    
    Optional<Voucher> findByCode(String code);
    
    List<Voucher> findByIsActiveTrue();
    
    List<Voucher> findByIsActiveFalse();
    
    List<Voucher> findByCodeContainingIgnoreCase(String code);
    
    List<Voucher> findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndIsActiveTrue(
        LocalDateTime startDate, LocalDateTime endDate);
    
    List<Voucher> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(
        LocalDateTime startDate, LocalDateTime endDate);
}

