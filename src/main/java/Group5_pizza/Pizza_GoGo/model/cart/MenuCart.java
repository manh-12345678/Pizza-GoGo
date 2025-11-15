package Group5_pizza.Pizza_GoGo.model.cart;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

import Group5_pizza.Pizza_GoGo.model.Voucher;
import lombok.Data;

@Data
public class MenuCart {

    private final Map<String, MenuCartItem> items = new LinkedHashMap<>();

    private Voucher voucher;
    private String voucherCode;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal voucherDiscount = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
        voucher = null;
        voucherCode = null;
        subtotal = BigDecimal.ZERO;
        voucherDiscount = BigDecimal.ZERO;
        total = BigDecimal.ZERO;
    }

    public void assignVoucher(Voucher voucher) {
        this.voucher = voucher;
        this.voucherCode = voucher != null ? voucher.getCode() : null;
    }

    public void removeVoucher() {
        this.voucher = null;
        this.voucherCode = null;
        recalculateTotals();
    }

    public void recalculateTotals() {
        subtotal = items.values().stream()
                .map(MenuCartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        voucherDiscount = calculateVoucherDiscount(subtotal);
        total = subtotal.subtract(voucherDiscount);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }
        total = total.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateVoucherDiscount(BigDecimal baseAmount) {
        if (voucher == null || baseAmount == null || baseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount = BigDecimal.ZERO;
        if (voucher.getDiscountPercent() != null
                && voucher.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
            discount = discount.add(
                    baseAmount.multiply(voucher.getDiscountPercent())
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
        }

        if (voucher.getDiscountAmount() != null
                && voucher.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            discount = discount.add(voucher.getDiscountAmount());
        }

        if (discount.compareTo(baseAmount) > 0) {
            discount = baseAmount;
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }
}

