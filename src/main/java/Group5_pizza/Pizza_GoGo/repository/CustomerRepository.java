package Group5_pizza.Pizza_GoGo.repository;

import Group5_pizza.Pizza_GoGo.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByPhone(String phone);
    
    Optional<Customer> findByEmail(String email);
    
    Optional<Customer> findByCustomerIdAndIsDeletedFalse(Integer customerId);
    
    @Query("SELECT c FROM Customer c WHERE c.customerId = :customerId AND c.isDeleted = false")
    Optional<Customer> findActiveCustomerById(@Param("customerId") Integer customerId);
}

