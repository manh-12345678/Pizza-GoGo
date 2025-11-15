package Group5_pizza.Pizza_GoGo.service;

import Group5_pizza.Pizza_GoGo.model.Account;
import Group5_pizza.Pizza_GoGo.model.Customer;
import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.Payment;
import Group5_pizza.Pizza_GoGo.repository.AccountRepository;
import Group5_pizza.Pizza_GoGo.repository.CustomerRepository;
import Group5_pizza.Pizza_GoGo.repository.OrderRepository;
import Group5_pizza.Pizza_GoGo.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEntityService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final OrderService orderService;
    private final RestaurantTableService tableService;

    /**
     * Tạo payment COD (Cash on Delivery)
     */
    @Transactional
    public Payment createCODPayment(Integer orderId, Integer accountId, String notes) {
        if (orderId == null) {
            throw new IllegalArgumentException("OrderId không được để trống");
        }
        if (accountId == null) {
            throw new IllegalArgumentException("AccountId không được để trống");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với ID: " + accountId));

        // Kiểm tra xem đã có payment pending cho order này chưa
        List<Payment> pendingPayments = paymentRepository.findPendingPaymentsByOrderId(orderId);
        if (!pendingPayments.isEmpty()) {
            Payment existingPayment = pendingPayments.get(0);
            String message = String.format(
                "Đơn hàng này đã có thanh toán COD đang chờ xử lý (Payment ID: %d). " +
                "Vui lòng hủy thanh toán cũ hoặc xác nhận thanh toán trước khi tạo mới.",
                existingPayment.getPaymentId()
            );
            throw new RuntimeException(message);
        }
        
        // Kiểm tra xem đã có payment completed cho order này chưa
        List<Payment> completedPayments = paymentRepository.findCompletedPaymentsByOrderId(orderId);
        if (!completedPayments.isEmpty()) {
            throw new RuntimeException("Đơn hàng này đã được thanh toán thành công. Không thể tạo thanh toán COD mới.");
        }

        // Tạo payment COD với status PENDING
        Payment payment = Payment.builder()
                .order(order)
                .account(account)
                .paymentMethod("COD")
                .amount(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                .status("PENDING")
                .notes(notes)
                .isDeleted(false)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        if (savedPayment != null) {
            log.info("Tạo payment COD thành công: PaymentId={}, OrderId={}, Amount={}", 
                    savedPayment.getPaymentId(), orderId, savedPayment.getAmount());
        }

        return savedPayment;
    }

    /**
     * Tạo payment VNPAY (trước khi redirect đến VNPAY gateway)
     */
    @Transactional
    public Payment createVnPayPayment(Integer orderId, Integer accountId, BigDecimal amount) {
        if (orderId == null) {
            throw new IllegalArgumentException("OrderId không được để trống");
        }
        if (accountId == null) {
            throw new IllegalArgumentException("AccountId không được để trống");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với ID: " + accountId));

        // Kiểm tra xem đã có payment pending cho order này chưa
        List<Payment> pendingPayments = paymentRepository.findPendingPaymentsByOrderId(orderId);
        if (!pendingPayments.isEmpty()) {
            Payment existingPayment = pendingPayments.get(0);
            if ("VNPAY".equals(existingPayment.getPaymentMethod())) {
                // Nếu đã có VNPAY payment pending, trả về payment đó
                log.info("Đã có VNPAY payment pending cho order: {}", orderId);
                return existingPayment;
            }
        }

        // Tạo payment VNPAY với status PENDING
        Payment payment = Payment.builder()
                .order(order)
                .account(account)
                .paymentMethod("VNPAY")
                .amount(amount != null ? amount : (order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO))
                .status("PENDING")
                .isDeleted(false)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        if (savedPayment != null) {
            log.info("Tạo payment VNPAY thành công: PaymentId={}, OrderId={}, Amount={}", 
                    savedPayment.getPaymentId(), orderId, savedPayment.getAmount());
        }

        return savedPayment;
    }

    /**
     * Xác nhận thanh toán COD (khi nhận hàng)
     */
    @Transactional
    public Payment confirmCODPayment(Integer paymentId, Integer accountId) {
        if (paymentId == null) {
            throw new IllegalArgumentException("PaymentId không được để trống");
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán với ID: " + paymentId));

        if (Boolean.TRUE.equals(payment.getIsDeleted())) {
            throw new RuntimeException("Thanh toán này đã bị xóa");
        }

        if (!"COD".equals(payment.getPaymentMethod())) {
            throw new RuntimeException("Chỉ có thể xác nhận thanh toán COD");
        }

        if (!"PENDING".equals(payment.getStatus())) {
            throw new RuntimeException("Chỉ có thể xác nhận thanh toán đang ở trạng thái PENDING");
        }

        // Cập nhật payment status
        payment.setStatus("COMPLETED");
        payment.setPaymentDate(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);

        // Cập nhật order
        Order order = payment.getOrder();
        if (order != null) {
            order.setPaidAmount(payment.getAmount());
            
            // Cập nhật status dựa trên loại đơn hàng
            // Đối với DELIVERY: sau khi thanh toán COD -> PROCESSING (chờ giao hàng)
            // Đối với DINE_IN/ONLINE: sau khi thanh toán COD -> COMPLETED
            String currentStatus = order.getStatus();
            if (!"COMPLETED".equals(currentStatus)) {
                if ("DELIVERY".equalsIgnoreCase(order.getOrderType())) {
                    // Đơn giao hàng: chuyển sang PROCESSING sau khi thanh toán
                    if (!"PROCESSING".equals(currentStatus) && !"COMPLETED".equals(currentStatus)) {
                        order.setStatus("PROCESSING");
                        log.info("Xác nhận thanh toán COD: OrderId={}, OrderType=DELIVERY, Status=PROCESSING", order.getOrderId());
                    }
                } else {
                    // Đơn tại quán hoặc online: chuyển sang COMPLETED sau khi thanh toán
                    order.setStatus("COMPLETED");
                    log.info("Xác nhận thanh toán COD: OrderId={}, OrderType={}, Status=COMPLETED", order.getOrderId(), order.getOrderType());
                }
            }
            
            orderService.saveOrder(order);
            
            // Nếu order COMPLETED và là DINE_IN, trả bàn về AVAILABLE
            if ("COMPLETED".equals(order.getStatus()) && "DINE_IN".equalsIgnoreCase(order.getOrderType())) {
                if (order.getTable() != null && order.getTable().getTableId() != null) {
                    try {
                        tableService.updateTableStatus(order.getTable().getTableId(), "AVAILABLE");
                        log.info("Đã trả bàn {} về AVAILABLE sau khi xác nhận thanh toán COD", order.getTable().getTableId());
                    } catch (Exception e) {
                        log.warn("Không thể cập nhật trạng thái bàn {}: {}", order.getTable().getTableId(), e.getMessage());
                    }
                }
            }
            
            // Tích điểm cho khách hàng (1% giá trị đơn hàng)
            addPointsToCustomer(order);
            
            log.info("Xác nhận thanh toán COD thành công: PaymentId={}, OrderId={}", 
                    paymentId, order.getOrderId());
        } else {
            log.warn("Không tìm thấy order cho payment: PaymentId={}", paymentId);
        }

        return savedPayment;
    }

    /**
     * Hủy thanh toán COD
     */
    @Transactional
    public Payment cancelCODPayment(Integer paymentId) {
        if (paymentId == null) {
            throw new IllegalArgumentException("PaymentId không được để trống");
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán với ID: " + paymentId));

        if (Boolean.TRUE.equals(payment.getIsDeleted())) {
            throw new RuntimeException("Thanh toán này đã bị xóa");
        }

        if ("COMPLETED".equals(payment.getStatus())) {
            throw new RuntimeException("Không thể hủy thanh toán đã hoàn thành");
        }

        payment.setStatus("CANCELLED");
        Payment savedPayment = paymentRepository.save(payment);

        log.info("Hủy thanh toán COD: PaymentId={}", paymentId);

        return savedPayment;
    }

    /**
     * Lấy tất cả payments
     */
    public List<Payment> getAllPayments() {
        return paymentRepository.findByIsDeletedFalse();
    }

    /**
     * Lấy payments theo orderId
     */
    public List<Payment> getPaymentsByOrderId(Integer orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("OrderId không được để trống");
        }
        return paymentRepository.findByOrderOrderIdAndIsDeletedFalse(orderId);
    }

    /**
     * Lấy payments theo accountId
     */
    public List<Payment> getPaymentsByAccountId(Integer accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("AccountId không được để trống");
        }
        return paymentRepository.findByAccountUserIdAndIsDeletedFalse(accountId);
    }

    /**
     * Lấy payment theo ID
     */
    public Payment getPaymentById(Integer paymentId) {
        if (paymentId == null) {
            throw new IllegalArgumentException("PaymentId không được để trống");
        }
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán với ID: " + paymentId));
    }

    /**
     * Xóa payment (soft delete)
     */
    @Transactional
    public void deletePayment(Integer paymentId) {
        if (paymentId == null) {
            throw new IllegalArgumentException("PaymentId không được để trống");
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán với ID: " + paymentId));

        payment.setIsDeleted(true);
        paymentRepository.save(payment);

        log.info("Xóa payment: PaymentId={}", paymentId);
    }

    /**
     * Tích điểm cho khách hàng khi thanh toán thành công (1% giá trị đơn hàng)
     */
    @Transactional
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

