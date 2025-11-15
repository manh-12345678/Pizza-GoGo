package Group5_pizza.Pizza_GoGo.DTO;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardSummaryDTO {
    @Builder.Default
    private final long totalOrders = 0L;

    @Builder.Default
    private final long pendingOrders = 0L;

    @Builder.Default
    private final long processingOrders = 0L;

    @Builder.Default
    private final long completedOrders = 0L;

    @Builder.Default
    private final long cancelledOrders = 0L;

    @Builder.Default
    private final long todayOrders = 0L;

    @Builder.Default
    private final long totalPayments = 0L;

    @Builder.Default
    private final long pendingPayments = 0L;

    @Builder.Default
    private final BigDecimal totalRevenue = BigDecimal.ZERO;

    @Builder.Default
    private final BigDecimal revenueToday = BigDecimal.ZERO;

    @Builder.Default
    private final long totalAccounts = 0L;

    @Builder.Default
    private final long staffCount = 0L;

    @Builder.Default
    private final long customerCount = 0L;

    @Builder.Default
    private final long totalTables = 0L;

    @Builder.Default
    private final long availableTables = 0L;

    @Builder.Default
    private final long occupiedTables = 0L;

    @Builder.Default
    private final long reservedTables = 0L;
}

