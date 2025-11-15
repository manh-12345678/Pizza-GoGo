package Group5_pizza.Pizza_GoGo.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import Group5_pizza.Pizza_GoGo.DTO.MenuComboView;
import Group5_pizza.Pizza_GoGo.DTO.MenuCategoryView;
import Group5_pizza.Pizza_GoGo.model.Account;
import Group5_pizza.Pizza_GoGo.model.Combo;
import Group5_pizza.Pizza_GoGo.model.ComboDetail;
import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.OrderDetail;
import Group5_pizza.Pizza_GoGo.model.Product;
import Group5_pizza.Pizza_GoGo.model.cart.CartSummary;
import Group5_pizza.Pizza_GoGo.model.cart.MenuCart;
import Group5_pizza.Pizza_GoGo.model.cart.MenuCartItem;
import Group5_pizza.Pizza_GoGo.service.ComboService;
import Group5_pizza.Pizza_GoGo.service.OrderService;
import Group5_pizza.Pizza_GoGo.service.PaymentEntityService;
import Group5_pizza.Pizza_GoGo.service.PaymentService;
import Group5_pizza.Pizza_GoGo.service.ProductService;
import Group5_pizza.Pizza_GoGo.service.ShippingService;
import Group5_pizza.Pizza_GoGo.service.ShoppingCartService;
import Group5_pizza.Pizza_GoGo.service.ShoppingCartService.ComboPricing;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/menu")
@RequiredArgsConstructor
@Slf4j
public class MenuController {

    private final ProductService productService;
    private final ComboService comboService;
    private final ShoppingCartService shoppingCartService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final PaymentEntityService paymentEntityService;
    private final ShippingService shippingService;

    @Value("${vnpay.returnUrl:}")
    private String vnPayReturnUrl;

    @GetMapping
    public String viewMenu(Model model, HttpSession session) {
        List<Product> products = productService.getAllProducts();
        List<MenuCategoryView> menuSections = buildMenuSections(products);

        List<Combo> availableCombos = comboService.getAllAvailableCombos();
        List<Combo> activeCombos = availableCombos.stream()
                .filter(this::isComboActive)
                .toList();
        if (activeCombos.isEmpty()) {
            activeCombos = availableCombos;
        }
        List<MenuComboView> comboViews = activeCombos.stream()
                .map(this::buildComboView)
                .toList();

        model.addAttribute("menuSections", menuSections);
        model.addAttribute("comboViews", comboViews);
        model.addAttribute("cartSummary", shoppingCartService.getSummary(session));
        model.addAttribute("navActive", "menu");

        return "menu/index";
    }

    @GetMapping("/cart")
    @ResponseBody
    public CartSummary getCart(HttpSession session) {
        return shoppingCartService.getSummary(session);
    }

    @PostMapping("/cart/add-product")
    @ResponseBody
    public ResponseEntity<CartSummary> addProduct(
            HttpSession session,
            @RequestParam Integer productId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        try {
            CartSummary summary = shoppingCartService.addProduct(session, productId, quantity);
            return ResponseEntity.ok(summary);
        } catch (Exception ex) {
            log.warn("Cannot add product to cart: {}", ex.getMessage());
            CartSummary summary = shoppingCartService.getSummary(session);
            summary.setMessage(ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(summary);
        }
    }

    @PostMapping("/cart/add-combo")
    @ResponseBody
    public ResponseEntity<CartSummary> addCombo(
            HttpSession session,
            @RequestParam Integer comboId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        try {
            CartSummary summary = shoppingCartService.addCombo(session, comboId, quantity);
            return ResponseEntity.ok(summary);
        } catch (Exception ex) {
            log.warn("Cannot add combo to cart: {}", ex.getMessage());
            CartSummary summary = shoppingCartService.getSummary(session);
            summary.setMessage(ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(summary);
        }
    }

    @PostMapping("/cart/update-quantity")
    @ResponseBody
    public ResponseEntity<CartSummary> updateQuantity(
            HttpSession session,
            @RequestParam String itemId,
            @RequestParam Integer quantity) {
        try {
            CartSummary summary = shoppingCartService.updateQuantity(session, itemId, quantity);
            return ResponseEntity.ok(summary);
        } catch (Exception ex) {
            log.warn("Cannot update cart item: {}", ex.getMessage());
            CartSummary summary = shoppingCartService.getSummary(session);
            summary.setMessage(ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(summary);
        }
    }

    @PostMapping("/cart/remove")
    @ResponseBody
    public ResponseEntity<CartSummary> removeItem(
            HttpSession session,
            @RequestParam String itemId) {
        try {
            CartSummary summary = shoppingCartService.removeItem(session, itemId);
            return ResponseEntity.ok(summary);
        } catch (Exception ex) {
            log.warn("Cannot remove cart item: {}", ex.getMessage());
            CartSummary summary = shoppingCartService.getSummary(session);
            summary.setMessage(ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(summary);
        }
    }

    @PostMapping("/cart/apply-voucher")
    @ResponseBody
    public ResponseEntity<CartSummary> applyVoucher(
            HttpSession session,
            @RequestParam String code) {
        try {
            CartSummary summary = shoppingCartService.applyVoucher(session, code);
            summary.setMessage("Áp dụng voucher thành công");
            return ResponseEntity.ok(summary);
        } catch (Exception ex) {
            log.warn("Cannot apply voucher: {}", ex.getMessage());
            CartSummary summary = shoppingCartService.getSummary(session);
            summary.setMessage(ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(summary);
        }
    }

    @PostMapping("/cart/remove-voucher")
    @ResponseBody
    public CartSummary removeVoucher(HttpSession session) {
        CartSummary summary = shoppingCartService.clearVoucher(session);
        summary.setMessage("Đã huỷ áp dụng voucher");
        return summary;
    }

    @PostMapping("/checkout")
    public String checkout(
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam("orderType") String orderType,
            @RequestParam(value = "shippingAddress", required = false) String shippingAddress,
            @RequestParam(value = "shippingContactName", required = false) String shippingContactName,
            @RequestParam(value = "shippingContactPhone", required = false) String shippingContactPhone,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        MenuCart cart = shoppingCartService.getCart(session);
        if (cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng của bạn đang trống");
            return "redirect:/menu";
        }

        Account loggedInUser = (Account) session.getAttribute("loggedInUser");
        if ("COD".equalsIgnoreCase(paymentMethod) && loggedInUser == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để thanh toán COD");
            return "redirect:/login";
        }

        // Validate shipping info nếu là DELIVERY
        if ("DELIVERY".equalsIgnoreCase(orderType)) {
            if (shippingAddress == null || shippingAddress.trim().isEmpty() ||
                shippingContactName == null || shippingContactName.trim().isEmpty() ||
                shippingContactPhone == null || shippingContactPhone.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng điền đầy đủ thông tin giao hàng");
                return "redirect:/menu";
            }
        }

        Order order = buildOrderFromCart(cart, loggedInUser);
        // Set order type từ form
        if (orderType != null && !orderType.isEmpty()) {
            order.setOrderType(orderType.toUpperCase());
        }
        order = orderService.saveOrder(order);

        // Tạo shipping record cho đơn hàng DELIVERY
        if ("DELIVERY".equalsIgnoreCase(orderType)) {
            try {
                shippingService.createShipping(order, shippingAddress.trim(), 
                    shippingContactName.trim(), shippingContactPhone.trim());
                log.info("Đã tạo shipping record cho order: {}", order.getOrderId());
            } catch (Exception e) {
                log.error("Lỗi khi tạo shipping record: {}", e.getMessage(), e);
                // Không fail checkout nếu shipping creation fails, chỉ log
            }
        }

        if ("VNPAY".equalsIgnoreCase(paymentMethod)) {
            session.setAttribute("clientIpAddress", getClientIpAddress(request));
            try {
                // KHÔNG tạo payment record trước khi redirect
                // Chỉ tạo payment khi callback từ VNPay thành công
                
                String returnUrl = resolveReturnUrl(request);
                String paymentUrl = paymentService.createVnPayPaymentUrl(
                        order.getOrderId(),
                        order.getTotalAmount(),
                        returnUrl,
                        session);
                shoppingCartService.clearCart(session);
                return "redirect:" + paymentUrl;
            } catch (Exception ex) {
                log.error("Error creating VNPAY URL: {}", ex.getMessage(), ex);
                redirectAttributes.addFlashAttribute("error", "Không thể tạo liên kết thanh toán VNPAY: " + ex.getMessage());
                return "redirect:/menu";
            }
        }

        if ("COD".equalsIgnoreCase(paymentMethod) && loggedInUser != null) {
            try {
                paymentEntityService.createCODPayment(order.getOrderId(), loggedInUser.getUserId(), null);
                redirectAttributes.addFlashAttribute("message",
                        "Đơn hàng #" + order.getOrderId() + " sẽ được thanh toán khi nhận hàng.");
                shoppingCartService.clearCart(session);
            } catch (Exception ex) {
                log.error("Error creating COD payment: {}", ex.getMessage(), ex);
                redirectAttributes.addFlashAttribute("error", "Không thể tạo thanh toán COD: " + ex.getMessage());
            }
            return "redirect:/orders/cart/" + order.getOrderId();
        }

        redirectAttributes.addFlashAttribute("error", "Phương thức thanh toán không hợp lệ");
        return "redirect:/menu";
    }

    private MenuComboView buildComboView(Combo combo) {
        Combo safeCombo = Objects.requireNonNull(combo, "combo must not be null");
        ComboPricing pricing = shoppingCartService.calculateComboPricing(safeCombo);
        String detailSummary = "";
        if (safeCombo.getComboDetails() != null) {
            detailSummary = safeCombo.getComboDetails().stream()
                    .filter(Objects::nonNull)
                    .map(detail -> {
                        Integer quantity = detail.getQuantity() != null ? detail.getQuantity() : 1;
                        String productName = (detail.getProduct() != null
                                && StringUtils.hasText(detail.getProduct().getName()))
                                ? detail.getProduct().getName()
                                : "Sản phẩm";
                        return quantity + "x " + productName;
                    })
                    .filter(StringUtils::hasText)
                    .collect(Collectors.joining(", "));
        }
        if (!StringUtils.hasText(detailSummary)) {
            detailSummary = "Combo gồm nhiều món hấp dẫn.";
        }
        return MenuComboView.builder()
                .combo(safeCombo)
                .details(safeCombo.getComboDetails())
                .basePrice(pricing.getBasePrice())
                .finalPrice(pricing.getFinalPrice())
                .discountAmount(pricing.getDiscountAmount())
                .discountPercent(pricing.getDiscountPercent())
                .description(detailSummary)
                .build();
    }

    private Order buildOrderFromCart(MenuCart cart, Account account) {
        Order order = new Order();
        // OrderType sẽ được set từ form trong checkout method
        order.setOrderType("DELIVERY"); // Default - chỉ có giao hàng
        order.setStatus("PENDING");
        // Set account (có thể null cho guest)
        order.setAccount(account);
        // Set customer nếu có (để tương thích với code cũ)
        order.setCustomer(account != null ? account.getCustomer() : null);

        for (MenuCartItem item : cart.getItems().values()) {
            if (item.getType() == null) {
                continue;
            }
            switch (item.getType()) {
                case PRODUCT -> addProductOrderDetail(order, item);
                case COMBO -> addComboOrderDetails(order, item);
                default -> log.warn("Unsupported cart item type: {}", item.getType());
            }
        }

        if (cart.getVoucher() != null) {
            order.setVoucher(cart.getVoucher());
        }
        order.recalculateTotal();
        return order;
    }

    private List<MenuCategoryView> buildMenuSections(List<Product> products) {
        List<Product> mainProducts = new ArrayList<>();
        List<Product> drinkProducts = new ArrayList<>();
        List<Product> beverageProducts = new ArrayList<>();
        Map<String, List<Product>> fallback = new LinkedHashMap<>();

        for (Product product : products) {
            if (product == null) {
                continue;
            }
            String categoryName = product.getCategory() != null
                    ? product.getCategory().getCategoryName()
                    : "Món khác";
            if (!StringUtils.hasText(categoryName)) {
                categoryName = "Món khác";
            }
            String normalized = normalize(categoryName);
            String normalizedName = normalize(product.getName());

            if (isMainCourse(normalized, normalizedName)) {
                mainProducts.add(product);
            } else if (isBeverage(normalized, normalizedName)) {
                beverageProducts.add(product);
            } else if (isDrink(normalized, normalizedName)) {
                drinkProducts.add(product);
            } else {
                fallback.computeIfAbsent(categoryName, key -> new ArrayList<>()).add(product);
            }
        }

        Map<String, List<Product>> ordered = new LinkedHashMap<>();
        if (!mainProducts.isEmpty()) {
            ordered.put("Pizza & Món chính", mainProducts);
        }
        if (!drinkProducts.isEmpty()) {
            ordered.put("Đồ uống", drinkProducts);
        }
        if (!beverageProducts.isEmpty()) {
            ordered.put("Nước uống", beverageProducts);
        }

        fallback.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .forEach(entry -> {
                    String label = StringUtils.hasText(entry.getKey()) ? entry.getKey() : "Món khác";
                    if (!ordered.containsKey(label)) {
                        ordered.put(label, entry.getValue());
                    } else {
                        ordered.computeIfAbsent(label + " khác", key -> new ArrayList<>()).addAll(entry.getValue());
                    }
                });

        if (ordered.isEmpty()) {
            ordered.put("Thực đơn", new ArrayList<>(products));
        }

        List<MenuCategoryView> views = new ArrayList<>();
        Map<String, Integer> slugCount = new LinkedHashMap<>();
        for (Map.Entry<String, List<Product>> entry : ordered.entrySet()) {
            String name = entry.getKey();
            String baseSlug = slugify(name);
            int count = slugCount.getOrDefault(baseSlug, 0);
            slugCount.put(baseSlug, count + 1);
            String slug = count == 0 ? baseSlug : baseSlug + "-" + count;

            views.add(MenuCategoryView.builder()
                    .name(name)
                    .slug(slug)
                    .products(entry.getValue())
                    .build());
        }
        return views;
    }

    private boolean isMainCourse(String normalizedCategory, String normalizedName) {
        return normalizedCategory.contains("pizza")
                || normalizedCategory.contains("mon chinh")
                || normalizedCategory.contains("burger")
                || normalizedCategory.contains("pasta")
                || normalizedCategory.contains("thuc an")
                || normalizedName.contains("pizza");
    }

    private boolean isDrink(String normalizedCategory, String normalizedName) {
        return normalizedCategory.contains("drink")
                || normalizedCategory.contains("do uong")
                || normalizedCategory.contains("coffee")
                || normalizedCategory.contains("tea")
                || normalizedCategory.contains("milk")
                || normalizedName.contains("coffee")
                || normalizedName.contains("tea");
    }

    private boolean isBeverage(String normalizedCategory, String normalizedName) {
        return normalizedCategory.contains("juice")
                || normalizedCategory.contains("nuoc")
                || normalizedCategory.contains("soda")
                || normalizedCategory.contains("beverage")
                || normalizedName.contains("juice")
                || normalizedName.contains("nuoc")
                || normalizedName.contains("soda");
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
    }

    private String slugify(String value) {
        String normalized = normalize(value);
        if (!StringUtils.hasText(normalized)) {
            return "category";
        }
        normalized = normalized.replaceAll("[^a-z0-9]+", "-");
        normalized = normalized.replaceAll("(^-|-$)", "");
        return StringUtils.hasText(normalized) ? normalized : "category";
    }

    private void addProductOrderDetail(Order order, MenuCartItem item) {
        if (item.getProductId() == null) {
            return;
        }
        Product product = productService.getProductById(item.getProductId());
        if (product == null) {
            return;
        }
        OrderDetail detail = new OrderDetail();
        detail.setOrder(order);
        detail.setProduct(product);
        detail.setQuantity(item.getQuantity());
        detail.setUnitPrice(defaultPrice(product.getPrice()));
        detail.setDiscount(BigDecimal.ZERO);
        detail.setNote(null);
        order.addOrderDetail(detail);
    }

    private void addComboOrderDetails(Order order, MenuCartItem item) {
        if (item.getComboId() == null) {
            return;
        }
        Combo combo = comboService.getComboById(item.getComboId());
        if (combo == null) {
            return;
        }
        BigDecimal discountPercent = combo.getDiscountPercent() != null
                ? combo.getDiscountPercent()
                : BigDecimal.ZERO;

        if (combo.getComboDetails() == null) {
            return;
        }

        for (ComboDetail detail : combo.getComboDetails()) {
            if (detail.getProduct() == null) {
                continue;
            }
            Product product = detail.getProduct();
            int detailQuantity = detail.getQuantity() != null ? detail.getQuantity() : 1;
            int totalQuantity = detailQuantity * item.getQuantity();

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setProduct(product);
            orderDetail.setQuantity(totalQuantity);
            BigDecimal unitPrice = defaultPrice(product.getPrice());
            orderDetail.setUnitPrice(unitPrice);
            orderDetail.setNote("Combo: " + combo.getName());

            if (discountPercent.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal base = unitPrice.multiply(BigDecimal.valueOf(totalQuantity));
                BigDecimal discountAmount = base.multiply(discountPercent)
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                orderDetail.setDiscount(discountAmount);
            } else {
                orderDetail.setDiscount(BigDecimal.ZERO);
            }

            order.addOrderDetail(orderDetail);
        }
    }

    private BigDecimal defaultPrice(BigDecimal price) {
        if (price == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return price.setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isComboActive(Combo combo) {
        if (combo == null || Boolean.TRUE.equals(combo.getIsDeleted())) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (combo.getStartDate() != null && now.isBefore(combo.getStartDate())) {
            return false;
        }
        if (combo.getEndDate() != null && now.isAfter(combo.getEndDate())) {
            return false;
        }
        return true;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor) && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp) && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    private String resolveReturnUrl(HttpServletRequest request) {
        if (StringUtils.hasText(vnPayReturnUrl)) {
            return vnPayReturnUrl;
        }
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        if (serverPort == 80 || serverPort == 443) {
            return String.format("%s://%s%s/payment/return", scheme, serverName, contextPath);
        }
        return String.format("%s://%s:%d%s/payment/return", scheme, serverName, serverPort, contextPath);
    }
}

