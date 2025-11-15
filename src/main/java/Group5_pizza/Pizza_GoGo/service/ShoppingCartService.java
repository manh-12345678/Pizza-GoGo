package Group5_pizza.Pizza_GoGo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import Group5_pizza.Pizza_GoGo.model.Combo;
import Group5_pizza.Pizza_GoGo.model.ComboDetail;
import Group5_pizza.Pizza_GoGo.model.Product;
import Group5_pizza.Pizza_GoGo.model.Voucher;
import Group5_pizza.Pizza_GoGo.model.cart.CartItemType;
import Group5_pizza.Pizza_GoGo.model.cart.CartSummary;
import Group5_pizza.Pizza_GoGo.model.cart.MenuCart;
import Group5_pizza.Pizza_GoGo.model.cart.MenuCartComboItem;
import Group5_pizza.Pizza_GoGo.model.cart.MenuCartItem;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShoppingCartService {

    public static final String SESSION_KEY = "MENU_CART";

    private final ProductService productService;
    private final ComboService comboService;
    private final VoucherService voucherService;

    public MenuCart getCart(HttpSession session) {
        MenuCart cart = (MenuCart) session.getAttribute(SESSION_KEY);
        if (cart == null) {
            cart = new MenuCart();
            session.setAttribute(SESSION_KEY, cart);
        }
        return cart;
    }

    public CartSummary getSummary(HttpSession session) {
        MenuCart cart = getCart(session);
        cart.recalculateTotals();
        return buildSummary(cart, null);
    }

    public CartSummary addProduct(HttpSession session, Integer productId, int quantity) {
        if (productId == null) {
            throw new IllegalArgumentException("Mã sản phẩm không hợp lệ");
        }
        if (quantity <= 0) {
            quantity = 1;
        }

        MenuCart cart = getCart(session);
        Product product = productService.getProductById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Sản phẩm không tồn tại");
        }
        if (Boolean.TRUE.equals(product.getIsDeleted())) {
            throw new IllegalArgumentException("Sản phẩm đã bị vô hiệu hoá");
        }

        BigDecimal unitPrice = defaultPrice(product.getPrice());
        String itemId = "PRODUCT_" + product.getProductId();
        MenuCartItem item = cart.getItems().get(itemId);
        if (item == null) {
            item = MenuCartItem.builder()
                    .id(itemId)
                    .type(CartItemType.PRODUCT)
                    .productId(product.getProductId())
                    .name(product.getName())
                    .quantity(0)
                    .unitPrice(unitPrice)
                    .originalUnitPrice(unitPrice)
                    .discountPerUnit(BigDecimal.ZERO)
                    .totalPrice(BigDecimal.ZERO)
                    .build();
            cart.getItems().put(itemId, item);
        }
        item.setQuantity(item.getQuantity() + quantity);
        item.setUnitPrice(unitPrice);
        item.setOriginalUnitPrice(unitPrice);
        item.setDiscountPerUnit(BigDecimal.ZERO);
        item.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP));

        cart.recalculateTotals();
        return buildSummary(cart, "Đã thêm " + product.getName() + " vào giỏ hàng");
    }

    public CartSummary addCombo(HttpSession session, Integer comboId, int quantity) {
        if (comboId == null) {
            throw new IllegalArgumentException("Mã combo không hợp lệ");
        }
        if (quantity <= 0) {
            quantity = 1;
        }

        MenuCart cart = getCart(session);
        Combo combo = comboService.getComboById(comboId);
        if (combo == null || Boolean.TRUE.equals(combo.getIsDeleted())) {
            throw new IllegalArgumentException("Combo không tồn tại hoặc đã bị vô hiệu hoá");
        }

        ComboPricing pricing = calculateComboPricing(combo);
        String itemId = "COMBO_" + combo.getComboId();
        MenuCartItem item = cart.getItems().get(itemId);
        if (item == null) {
            item = MenuCartItem.builder()
                    .id(itemId)
                    .type(CartItemType.COMBO)
                    .comboId(combo.getComboId())
                    .name(combo.getName())
                    .quantity(0)
                    .comboItems(new ArrayList<>())
                    .build();
            cart.getItems().put(itemId, item);
        }

        List<MenuCartComboItem> components = new ArrayList<>();
        if (combo.getComboDetails() != null) {
            for (ComboDetail detail : combo.getComboDetails()) {
                if (detail == null || detail.getProduct() == null) {
                    continue;
                }
                Product product = detail.getProduct();
                components.add(MenuCartComboItem.builder()
                        .productId(product.getProductId())
                        .productName(product.getName())
                        .quantity(detail.getQuantity() != null ? detail.getQuantity() : 1)
                        .unitPrice(defaultPrice(product.getPrice()))
                        .build());
            }
        }

        item.setQuantity(item.getQuantity() + quantity);
        item.setUnitPrice(pricing.getFinalPrice());
        item.setOriginalUnitPrice(pricing.getBasePrice());
        item.setDiscountPerUnit(pricing.getDiscountAmount());
        item.setTotalPrice(pricing.getFinalPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP));
        item.setComboItems(components);

        cart.recalculateTotals();
        return buildSummary(cart, "Đã thêm combo " + combo.getName() + " vào giỏ hàng");
    }

    public CartSummary updateQuantity(HttpSession session, String itemId, int quantity) {
        if (!StringUtils.hasText(itemId)) {
            throw new IllegalArgumentException("Mã mục giỏ hàng không hợp lệ");
        }
        MenuCart cart = getCart(session);
        MenuCartItem item = cart.getItems().get(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Không tìm thấy sản phẩm trong giỏ");
        }

        String message;
        if (quantity <= 0) {
            message = "Đã xoá " + item.getName() + " khỏi giỏ hàng";
            cart.getItems().remove(itemId);
        } else {
            item.setQuantity(quantity);
            item.setTotalPrice(item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(quantity))
                    .setScale(2, RoundingMode.HALF_UP));
            message = "Đã cập nhật " + item.getName() + " x" + quantity;
        }

        cart.recalculateTotals();
        return buildSummary(cart, message);
    }

    public CartSummary removeItem(HttpSession session, String itemId) {
        if (!StringUtils.hasText(itemId)) {
            throw new IllegalArgumentException("Mã mục giỏ hàng không hợp lệ");
        }
        MenuCart cart = getCart(session);
        MenuCartItem removed = cart.getItems().remove(itemId);
        cart.recalculateTotals();
        String name = removed != null ? removed.getName() : "sản phẩm";
        return buildSummary(cart, "Đã xoá " + name + " khỏi giỏ hàng");
    }

    public CartSummary applyVoucher(HttpSession session, String code) {
        if (!StringUtils.hasText(code)) {
            throw new IllegalArgumentException("Vui lòng nhập mã voucher");
        }
        MenuCart cart = getCart(session);
        String normalized = code.trim().toUpperCase();
        Voucher voucher = voucherService.getVoucherByCode(normalized);
        if (!voucherService.validateVoucher(normalized)) {
            throw new IllegalArgumentException("Voucher không hợp lệ hoặc đã hết hạn");
        }

        cart.assignVoucher(voucher);
        cart.recalculateTotals();
        return buildSummary(cart, "Áp dụng voucher " + voucher.getCode() + " thành công");
    }

    public CartSummary clearVoucher(HttpSession session) {
        MenuCart cart = getCart(session);
        cart.removeVoucher();
        return buildSummary(cart, "Đã huỷ áp dụng voucher");
    }

    public void clearCart(HttpSession session) {
        session.removeAttribute(SESSION_KEY);
    }

    public ComboPricing calculateComboPricing(Combo combo) {
        BigDecimal basePrice = BigDecimal.ZERO;
        if (combo.getComboDetails() != null) {
            for (ComboDetail detail : combo.getComboDetails()) {
                if (detail == null || detail.getProduct() == null) {
                    continue;
                }
                BigDecimal price = defaultPrice(detail.getProduct().getPrice());
                int qty = detail.getQuantity() != null ? detail.getQuantity() : 1;
                basePrice = basePrice.add(price.multiply(BigDecimal.valueOf(qty)));
            }
        }
        basePrice = basePrice.setScale(2, RoundingMode.HALF_UP);

        BigDecimal discountPercent = combo.getDiscountPercent() != null
                ? combo.getDiscountPercent()
                : BigDecimal.ZERO;
        if (discountPercent.compareTo(BigDecimal.ZERO) < 0) {
            discountPercent = BigDecimal.ZERO;
        }

        BigDecimal discountAmount = basePrice.multiply(discountPercent)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        BigDecimal finalPrice = basePrice.subtract(discountAmount);
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO;
        }
        finalPrice = finalPrice.setScale(2, RoundingMode.HALF_UP);

        return new ComboPricing(basePrice, finalPrice, discountAmount, discountPercent);
    }

    private BigDecimal defaultPrice(BigDecimal price) {
        if (price == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return price.setScale(2, RoundingMode.HALF_UP);
    }

    private CartSummary buildSummary(MenuCart cart, String message) {
        CartSummary summary = CartSummary.from(cart);
        summary.setMessage(message);
        return summary;
    }

    @Getter
    @AllArgsConstructor
    public static class ComboPricing {
        private final BigDecimal basePrice;
        private final BigDecimal finalPrice;
        private final BigDecimal discountAmount;
        private final BigDecimal discountPercent;
    }
}

