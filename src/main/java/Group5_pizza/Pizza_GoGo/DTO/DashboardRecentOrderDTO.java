package Group5_pizza.Pizza_GoGo.DTO;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DashboardRecentOrderDTO {
    private final Integer orderId;
    private final String tableName;
    private final String status;
    private final BigDecimal totalAmount;
    private final LocalDateTime createdAt;

    public String getDisplayTableName() {
        return tableName != null ? tableName : "Mang đi";
    }

    public String getFormattedCreatedAt() {
        return createdAt != null
                ? createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "--/--/---- --:--";
    }

    public String getFormattedTotalAmount() {
        if (totalAmount == null) {
            return "0 ₫";
        }
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        formatter.setMaximumFractionDigits(0);
        formatter.setMinimumFractionDigits(0);
        return formatter.format(totalAmount) + " ₫";
    }
}

