// package Group5_pizza.Pizza_GoGo.DTO;
// OrderResponseDTO.java
package Group5_pizza.Pizza_GoGo.DTO;
import Group5_pizza.Pizza_GoGo.model.Order;
import Group5_pizza.Pizza_GoGo.model.OrderDetail;
import Group5_pizza.Pizza_GoGo.model.OrderDetailTopping;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
@Data
public class OrderResponseDTO {
    private Integer orderId;
    private Integer tableId;
    private String tableName;
    private String orderType;
    private String time;
    private String status;
    private BigDecimal totalAmount;
    private List<OrderItemDTO> items;
    private PaymentInfoDTO paymentInfo; // Thông tin thanh toán
    
    public OrderResponseDTO(Order order) {
        try {
            this.orderId = order.getOrderId();
            this.tableId = order.getTable() != null ? order.getTable().getTableId() : null;
            this.tableName = order.getTable() != null ? order.getTable().getTableName() : "Bàn trống";
            this.orderType = order.getOrderType();
            this.time = order.getFormattedCreatedAt();
            this.status = order.getStatus();
            this.totalAmount = order.getTotalAmount();
            
            // Load order details
            if (order.getOrderDetails() != null) {
                this.items = order.getOrderDetails().stream()
                        .filter(d -> d != null && !Boolean.TRUE.equals(d.getIsDeleted()))
                        .map(OrderItemDTO::new)
                        .collect(Collectors.toList());
            } else {
                this.items = List.of();
            }
            
            // Load payment info nếu có
            try {
                if (order.getPayments() != null && !order.getPayments().isEmpty()) {
                    this.paymentInfo = order.getPayments().stream()
                            .filter(p -> p != null && !Boolean.TRUE.equals(p.getIsDeleted()))
                            .findFirst()
                            .map(PaymentInfoDTO::new)
                            .orElse(null);
                }
            } catch (Exception e) {
                // Nếu có lỗi khi load payments, bỏ qua
                this.paymentInfo = null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi convert Order sang DTO: " + e.getMessage(), e);
        }
    }
    // NESTED CLASS - PUBLIC
    @Data
    public static class OrderItemDTO {
        private Integer orderDetailId;
        private Integer productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private String note;
        private List<ToppingDTO> toppings;
        public OrderItemDTO(OrderDetail detail) {
            this.orderDetailId = detail.getOrderDetailId();
            this.productId = detail.getProduct() != null ? detail.getProduct().getProductId() : null;
            this.productName = detail.getProduct() != null ? detail.getProduct().getName() : "Unknown";
            this.quantity = detail.getQuantity();
            this.unitPrice = detail.getUnitPrice();
            this.subtotal = detail.getSubtotal();
            this.note = detail.getNote();
            // Map toppings từ orderDetailToppings
            if (detail.getOrderDetailToppings() != null && !detail.getOrderDetailToppings().isEmpty()) {
                this.toppings = detail.getOrderDetailToppings().stream()
                        .filter(t -> t != null && !Boolean.TRUE.equals(t.getIsDeleted()) && t.getTopping() != null)
                        .map(ToppingDTO::new)
                        .collect(Collectors.toList());
            } else {
                this.toppings = List.of();
            }
        }
    }
    // NESTED CLASS - PUBLIC
    @Data
    public static class ToppingDTO {
        private Integer toppingId;
        private String name;
        private BigDecimal price;
        public ToppingDTO(OrderDetailTopping t) {
            if (t == null) {
                throw new IllegalArgumentException("OrderDetailTopping cannot be null");
            }
            this.toppingId = t.getOrderDetailToppingId();
            this.name = t.getTopping() != null ? t.getTopping().getName() : "Unknown";
            this.price = t.getPrice() != null ? t.getPrice() : BigDecimal.ZERO;
        }
    }
    
    // NESTED CLASS - Payment Info
    @Data
    public static class PaymentInfoDTO {
        private Integer paymentId;
        private String paymentMethod;
        private String status;
        private BigDecimal amount;
        
        public PaymentInfoDTO(Group5_pizza.Pizza_GoGo.model.Payment payment) {
            this.paymentId = payment.getPaymentId();
            this.paymentMethod = payment.getPaymentMethod();
            this.status = payment.getStatus();
            this.amount = payment.getAmount();
        }
    }
}