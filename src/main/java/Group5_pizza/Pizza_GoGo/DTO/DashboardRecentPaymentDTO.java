package Group5_pizza.Pizza_GoGo.DTO;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DashboardRecentPaymentDTO {
    private final Integer paymentId;
    private final Integer orderId;
    private final String paymentMethod;
    private final String status;
    private final BigDecimal amount;
    private final LocalDateTime createdAt;

    public String getFormattedCreatedAt() {
        return createdAt != null
                ? createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "--/--/---- --:--";
    }

    public String getFormattedAmount() {
        if (amount == null) {
            return "0 ₫";
        }
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        formatter.setMaximumFractionDigits(0);
        formatter.setMinimumFractionDigits(0);
        return formatter.format(amount) + " ₫";
    }
}

