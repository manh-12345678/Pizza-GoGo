package Group5_pizza.Pizza_GoGo.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import Group5_pizza.Pizza_GoGo.DTO.DashboardRecentOrderDTO;
import Group5_pizza.Pizza_GoGo.DTO.DashboardRecentPaymentDTO;
import Group5_pizza.Pizza_GoGo.DTO.DashboardSummaryDTO;
import Group5_pizza.Pizza_GoGo.repository.AccountRepository;
import Group5_pizza.Pizza_GoGo.repository.OrderRepository;
import Group5_pizza.Pizza_GoGo.repository.PaymentRepository;
import Group5_pizza.Pizza_GoGo.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM", new Locale("vi", "VN"));

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final RestaurantTableRepository tableRepository;

    public DashboardSummaryDTO getSummary() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        long totalOrders = orderRepository.countByIsDeletedFalse();
        long pendingOrders = orderRepository.countByStatusIgnoreCaseAndIsDeletedFalse("PENDING");
        long processingOrders = orderRepository.countByStatusIgnoreCaseAndIsDeletedFalse("PROCESSING");
        long completedOrders = orderRepository.countByStatusIgnoreCaseAndIsDeletedFalse("COMPLETED");
        long cancelledOrders = orderRepository.countByStatusIgnoreCaseAndIsDeletedFalse("CANCELLED");
        long todayOrders = orderRepository.countByCreatedAtBetweenAndIsDeletedFalse(startOfDay, endOfDay);

        long totalPayments = paymentRepository.countByIsDeletedFalse();
        long pendingPayments = paymentRepository.countByStatusAndIsDeletedFalse("PENDING");

        BigDecimal totalRevenue = defaultZero(paymentRepository.sumCompletedPayments());
        BigDecimal revenueToday = defaultZero(paymentRepository.sumCompletedPaymentsBetween(startOfDay, endOfDay));

        long totalAccounts = accountRepository.countByIsDeletedFalse();
        long staffCount = accountRepository.countByRoleRoleNameIgnoreCaseAndIsDeletedFalse("STAFF");
        long customerCount = accountRepository.countByRoleRoleNameIgnoreCaseAndIsDeletedFalse("CUSTOMER");

        long totalTables = tableRepository.countByIsDeletedFalse();
        long availableTables = tableRepository.countByStatusIgnoreCaseAndIsDeletedFalse("AVAILABLE");
        long occupiedTables = tableRepository.countByStatusIgnoreCaseAndIsDeletedFalse("OCCUPIED");
        long reservedTables = tableRepository.countByStatusIgnoreCaseAndIsDeletedFalse("RESERVED");

        return DashboardSummaryDTO.builder()
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .processingOrders(processingOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .todayOrders(todayOrders)
                .totalPayments(totalPayments)
                .pendingPayments(pendingPayments)
                .totalRevenue(totalRevenue)
                .revenueToday(revenueToday)
                .totalAccounts(totalAccounts)
                .staffCount(staffCount)
                .customerCount(customerCount)
                .totalTables(totalTables)
                .availableTables(availableTables)
                .occupiedTables(occupiedTables)
                .reservedTables(reservedTables)
                .build();
    }

    public Map<String, Long> getOrderStatusDistribution() {
        Map<String, Long> distribution = new LinkedHashMap<>();
        distribution.put("PENDING", orderRepository.countByStatusIgnoreCaseAndIsDeletedFalse("PENDING"));
        distribution.put("PROCESSING", orderRepository.countByStatusIgnoreCaseAndIsDeletedFalse("PROCESSING"));
        distribution.put("COMPLETED", orderRepository.countByStatusIgnoreCaseAndIsDeletedFalse("COMPLETED"));
        distribution.put("CANCELLED", orderRepository.countByStatusIgnoreCaseAndIsDeletedFalse("CANCELLED"));
        return distribution;
    }

    public Map<String, Long> getPaymentStatusDistribution() {
        Map<String, Long> distribution = new LinkedHashMap<>();
        distribution.put("PENDING", paymentRepository.countByStatusAndIsDeletedFalse("PENDING"));
        distribution.put("COMPLETED", paymentRepository.countByStatusAndIsDeletedFalse("COMPLETED"));
        distribution.put("FAILED", paymentRepository.countByStatusAndIsDeletedFalse("FAILED"));
        distribution.put("CANCELLED", paymentRepository.countByStatusAndIsDeletedFalse("CANCELLED"));
        return distribution;
    }

    public Map<String, Long> getTableStatusDistribution() {
        Map<String, Long> distribution = new LinkedHashMap<>();
        distribution.put("AVAILABLE", tableRepository.countByStatusIgnoreCaseAndIsDeletedFalse("AVAILABLE"));
        distribution.put("RESERVED", tableRepository.countByStatusIgnoreCaseAndIsDeletedFalse("RESERVED"));
        distribution.put("OCCUPIED", tableRepository.countByStatusIgnoreCaseAndIsDeletedFalse("OCCUPIED"));
        return distribution;
    }

    public List<DashboardRecentOrderDTO> getRecentOrders(int limit) {
        return orderRepository.findRecentOrderSummaries(PageRequest.of(0, Math.max(limit, 1)));
    }

    public List<DashboardRecentPaymentDTO> getRecentPayments(int limit) {
        return paymentRepository.findRecentPaymentSummaries(PageRequest.of(0, Math.max(limit, 1)));
    }

    public Map<String, Long> getOrdersLast7Days() {
        LocalDate today = LocalDate.now();
        Map<String, Long> result = new LinkedHashMap<>();
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            
            long count = orderRepository.countByCreatedAtBetweenAndIsDeletedFalse(startOfDay, endOfDay);
            result.put(date.format(DAY_FORMATTER), count);
        }

        return result;
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}

