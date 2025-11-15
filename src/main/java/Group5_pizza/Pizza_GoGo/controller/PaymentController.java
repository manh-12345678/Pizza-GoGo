package Group5_pizza.Pizza_GoGo.controller;

import Group5_pizza.Pizza_GoGo.model.Account;
import Group5_pizza.Pizza_GoGo.model.Customer;
import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.Payment;
import Group5_pizza.Pizza_GoGo.model.PaymentResponse;
import Group5_pizza.Pizza_GoGo.repository.CustomerRepository;
import Group5_pizza.Pizza_GoGo.repository.PaymentRepository;
import Group5_pizza.Pizza_GoGo.service.OrderService;
import Group5_pizza.Pizza_GoGo.service.PaymentEntityService;
import Group5_pizza.Pizza_GoGo.service.PaymentService;
import Group5_pizza.Pizza_GoGo.service.RestaurantTableService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentEntityService paymentEntityService;
    private final OrderService orderService;
    private final RestaurantTableService tableService;
    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;

    @Value("${vnpay.returnUrl:}")
    private String vnPayReturnUrl;

    // Khởi tạo thanh toán
    @PostMapping("/create")
    public String createPayment(
            @RequestParam Integer orderId,
            HttpServletRequest request,
            HttpSession session,
            Model model) {
        try {
            Order order = orderService.getOrderWithDetails(orderId);
            if (order == null) {
                model.addAttribute("error", "Đơn hàng không tồn tại");
                return "payment/error";
            }

            // Lưu IP address của client
            String clientIp = getClientIpAddress(request);
            session.setAttribute("clientIpAddress", clientIp);

            // Tạo return URL - đảm bảo đầy đủ protocol và domain
            String scheme = request.getScheme(); // http hoặc https
            String serverName = request.getServerName(); // localhost
            int serverPort = request.getServerPort(); // 8080
            String contextPath = request.getContextPath(); // có thể là rỗng
            
            String returnUrl;
            if (StringUtils.hasText(vnPayReturnUrl)) {
                returnUrl = vnPayReturnUrl;
            } else {
                if (serverPort == 80 || serverPort == 443) {
                    returnUrl = String.format("%s://%s%s/payment/return", scheme, serverName, contextPath);
                } else {
                    returnUrl = String.format("%s://%s:%d%s/payment/return", scheme, serverName, serverPort, contextPath);
                }
            }

            // Tạo payment URL
            BigDecimal amount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
            String paymentUrl = paymentService.createVnPayPaymentUrl(
                    orderId,
                    amount,
                    returnUrl,
                    session
            );

            log.info("Tạo payment URL cho đơn hàng {}: {}", orderId, paymentUrl);
            return "redirect:" + paymentUrl;

        } catch (Exception e) {
            log.error("Lỗi khi tạo payment URL: {}", e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi tạo link thanh toán: " + e.getMessage());
            return "payment/error";
        }
    }

    // Xử lý callback từ VNPAY
    @GetMapping("/return")
    public String paymentReturn(
            @RequestParam Map<String, String> allParams,
            Model model) {
        try {
            log.info("Nhận callback từ VNPAY: {}", allParams);

            // Xử lý response từ VNPAY
            PaymentResponse paymentResponse = paymentService.processVnPayReturn(allParams);

            if (paymentResponse.isSuccess()) {
                // Cập nhật trạng thái đơn hàng
                Integer orderId = paymentResponse.getOrderId();
                
                if (orderId == null) {
                    log.warn("Payment thành công nhưng không có orderId");
                    model.addAttribute("error", "Thanh toán thành công nhưng không tìm thấy thông tin đơn hàng");
                    return "payment/error";
                }
                
                Order order = orderService.getOrderWithDetails(orderId);
                
                if (order == null) {
                    log.warn("Payment thành công nhưng không tìm thấy order với ID: {}", orderId);
                    model.addAttribute("error", "Thanh toán thành công nhưng không tìm thấy đơn hàng");
                    model.addAttribute("orderId", orderId);
                    return "payment/error";
                }
                
                // Tạo payment record mới khi callback thành công (chỉ khi thanh toán thành công)
                // Không tạo payment trước khi redirect để tránh lưu payment khi thanh toán lỗi
                List<Payment> payments = paymentRepository.findByOrderOrderIdAndIsDeletedFalse(orderId);
                Payment vnpayPayment = payments.stream()
                        .filter(p -> "VNPAY".equals(p.getPaymentMethod()))
                        .findFirst()
                        .orElse(null);
                
                if (vnpayPayment == null) {
                    // Tạo payment mới khi callback thành công (chỉ khi thanh toán thành công)
                    Account account = order.getAccount();
                    if (account != null) {
                        // Tạo payment với status COMPLETED ngay (vì đã thanh toán thành công)
                        Payment newPayment = Payment.builder()
                                .order(order)
                                .account(account)
                                .paymentMethod("VNPAY")
                                .amount(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                                .status("COMPLETED") // Tạo với status COMPLETED ngay
                                .paymentDate(LocalDateTime.now())
                                .transactionId(paymentResponse.getTransactionId())
                                .isDeleted(false)
                                .build();
                        vnpayPayment = paymentRepository.save(newPayment);
                        log.info("Đã tạo payment record mới khi callback thành công: PaymentId={}, OrderId={}, Status=COMPLETED", 
                                vnpayPayment.getPaymentId(), orderId);
                    } else {
                        log.warn("Không thể tạo payment: Order {} không có account", orderId);
                    }
                } else {
                    // Nếu đã có payment (trường hợp hiếm), cập nhật status và transaction ID
                    vnpayPayment.setStatus("COMPLETED");
                    vnpayPayment.setPaymentDate(LocalDateTime.now());
                    if (paymentResponse.getTransactionId() != null) {
                        vnpayPayment.setTransactionId(paymentResponse.getTransactionId());
                    }
                    paymentRepository.save(vnpayPayment);
                    log.info("Đã cập nhật payment record: PaymentId={}, OrderId={}, Status=COMPLETED", 
                            vnpayPayment.getPaymentId(), orderId);
                }
                
                // Cập nhật trạng thái đơn hàng và số tiền đã thanh toán
                if (order.getTotalAmount() != null) {
                    order.setPaidAmount(order.getTotalAmount());
                }
                
                // Cập nhật status dựa trên loại đơn hàng
                // Đối với DELIVERY: sau khi thanh toán -> PROCESSING (chờ giao hàng)
                // Đối với DINE_IN/ONLINE: sau khi thanh toán -> COMPLETED
                String currentStatus = order.getStatus();
                if (!"COMPLETED".equals(currentStatus)) {
                    if ("DELIVERY".equalsIgnoreCase(order.getOrderType())) {
                        // Đơn giao hàng: chuyển sang PROCESSING sau khi thanh toán
                        if (!"PROCESSING".equals(currentStatus) && !"COMPLETED".equals(currentStatus)) {
                            order.setStatus("PROCESSING");
                            log.info("Đã cập nhật order: OrderId={}, OrderType=DELIVERY, Status=PROCESSING", orderId);
                        }
                    } else {
                        // Đơn tại quán hoặc online: chuyển sang COMPLETED sau khi thanh toán
                        order.setStatus("COMPLETED");
                        log.info("Đã cập nhật order: OrderId={}, OrderType={}, Status=COMPLETED", orderId, order.getOrderType());
                    }
                }
                orderService.saveOrder(order);
                
                // Nếu order COMPLETED và là DINE_IN, trả bàn về AVAILABLE
                if ("COMPLETED".equals(order.getStatus()) && "DINE_IN".equalsIgnoreCase(order.getOrderType())) {
                    if (order.getTable() != null && order.getTable().getTableId() != null) {
                        try {
                            tableService.updateTableStatus(order.getTable().getTableId(), "AVAILABLE");
                            log.info("Đã trả bàn {} về AVAILABLE sau khi thanh toán thành công", order.getTable().getTableId());
                        } catch (Exception e) {
                            log.warn("Không thể cập nhật trạng thái bàn {}: {}", order.getTable().getTableId(), e.getMessage());
                        }
                    }
                }
                
                // Tích điểm cho khách hàng (1% giá trị đơn hàng)
                addPointsToCustomer(order);

                model.addAttribute("orderId", orderId);
                model.addAttribute("transactionInfo", paymentResponse.getTransactionInfo());
                model.addAttribute("order", order);
                return "payment/success";
            } else {
                model.addAttribute("error", paymentResponse.getMessage());
                model.addAttribute("orderId", paymentResponse.getOrderId());
                return "payment/failure";
            }

        } catch (Exception e) {
            log.error("Lỗi khi xử lý payment return: {}", e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi xử lý kết quả thanh toán: " + e.getMessage());
            return "payment/error";
        }
    }

    // Lấy IP address của client
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    // Tạo thanh toán COD
    @PostMapping("/cod/create")
    public String createCODPayment(
            @RequestParam Integer orderId,
            @RequestParam(required = false) String notes,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            Account loggedInUser = (Account) session.getAttribute("loggedInUser");
            if (loggedInUser == null) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để thanh toán");
                return "redirect:/login";
            }

            paymentEntityService.createCODPayment(orderId, loggedInUser.getUserId(), notes);
            redirectAttributes.addFlashAttribute("message", "Đã tạo thanh toán COD thành công. Vui lòng thanh toán khi nhận hàng.");
            return "redirect:/orders/cart/" + orderId;

        } catch (Exception e) {
            log.error("Lỗi khi tạo thanh toán COD: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tạo thanh toán COD: " + e.getMessage());
            return "redirect:/orders/cart/" + orderId;
        }
    }

    /**
     * Tích điểm cho khách hàng khi thanh toán thành công (1% giá trị đơn hàng)
     */
    private void addPointsToCustomer(Order order) {
        if (order == null || order.getCustomer() == null) {
            return;
        }

        try {
            Customer customer = customerRepository.findById(order.getCustomer().getCustomerId()).orElse(null);
            if (customer == null) {
                return;
            }

            if (customer.getPoints() == null) {
                customer.setPoints(0);
            }

            // Tính điểm: 1% giá trị đơn hàng (làm tròn)
            BigDecimal totalAmount = order.getTotalAmount();
            if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return;
            }

            // 1% = totalAmount / 100, làm tròn xuống
            int pointsToAdd = totalAmount.divide(new BigDecimal("100"), 0, RoundingMode.DOWN).intValue();
            
            if (pointsToAdd > 0) {
                customer.setPoints(customer.getPoints() + pointsToAdd);
                customerRepository.save(customer);
                log.info("Đã tích {} điểm cho customer {} (OrderId: {})", 
                        pointsToAdd, customer.getCustomerId(), order.getOrderId());
            }
        } catch (Exception e) {
            log.error("Lỗi khi tích điểm cho khách hàng: {}", e.getMessage(), e);
            // Không fail payment nếu tích điểm lỗi
        }
    }
}

