package Group5_pizza.Pizza_GoGo.model;

import lombok.Data;

@Data
public class PaymentResponse {
    private boolean success;
    private String message;
    private Integer orderId;
    private String transactionInfo;
    private String transactionId; // Mã giao dịch từ VNPAY

    public static PaymentResponse success(Integer orderId, String transactionInfo) {
        PaymentResponse response = new PaymentResponse();
        response.setSuccess(true);
        response.setOrderId(orderId);
        response.setTransactionInfo(transactionInfo);
        return response;
    }

    public static PaymentResponse success(Integer orderId, String transactionInfo, String transactionId) {
        PaymentResponse response = new PaymentResponse();
        response.setSuccess(true);
        response.setOrderId(orderId);
        response.setTransactionInfo(transactionInfo);
        response.setTransactionId(transactionId);
        return response;
    }

    public static PaymentResponse failure(String message) {
        PaymentResponse response = new PaymentResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    public static PaymentResponse failure(Integer orderId, String message) {
        PaymentResponse response = new PaymentResponse();
        response.setSuccess(false);
        response.setOrderId(orderId);
        response.setMessage(message);
        return response;
    }
}