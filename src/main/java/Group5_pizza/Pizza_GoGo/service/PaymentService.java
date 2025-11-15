package Group5_pizza.Pizza_GoGo.service;

import Group5_pizza.Pizza_GoGo.model.PaymentResponse;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PaymentService {

    @Value("${vnpay.tmnCode}")
    private String vnpTmnCode;

    @Value("${vnpay.hashSecret}")
    private String vnpHashSecret;

    @Value("${vnpay.paymentUrl}")
    private String vnpPaymentUrl;

    @Value("${vnpay.version}")
    private String vnpVersion;

    @Value("${vnpay.command}")
    private String vnpCommand;

    @Value("${vnpay.currCode}")
    private String vnpCurrCode;

    @Value("${vnpay.locale}")
    private String vnpLocale;

    public String createVnPayPaymentUrl(
            Integer orderId,
            BigDecimal amount,
            String returnUrl,
            HttpSession session) throws Exception {

        // Tạo transaction reference - đảm bảo unique và format đúng
        // Format: orderId_timestamp (tối đa 100 ký tự)
        String vnp_TxnRef = orderId + "_" + System.currentTimeMillis();
        
        // Convert amount to VND (nhân 100 để bỏ phần thập phân)
        long vnp_Amount = amount.multiply(new BigDecimal("100")).longValue();
        
        // Kiểm tra amount hợp lệ
        if (vnp_Amount <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0");
        }
        // Một số môi trường sandbox cho phép mức thấp, hạ mức kiểm tra tối thiểu xuống 2,000 VND
        if (vnp_Amount < 200000) { // 2,000 VND x 100
            throw new IllegalArgumentException("Số tiền thanh toán tối thiểu là 2,000 VND");
        }

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnpVersion);
        vnp_Params.put("vnp_Command", vnpCommand);
        vnp_Params.put("vnp_TmnCode", vnpTmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(vnp_Amount));
        vnp_Params.put("vnp_CurrCode", vnpCurrCode);

        // Generate unique order ID with format YYYYMMDDHHMMSSxxxx
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        // Save IP address of customer
        String vnp_IpAddr = (String) session.getAttribute("clientIpAddress");
        if (vnp_IpAddr == null || vnp_IpAddr.isEmpty()) {
            vnp_IpAddr = "127.0.0.1"; // Default if not available
        }
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        // Set locale
        vnp_Params.put("vnp_Locale", vnpLocale);

        // Add order info - chỉ dùng số và chữ, không có ký tự đặc biệt (tối đa 255 ký tự)
        String orderInfo = "Thanh toan don hang " + orderId;
        if (orderInfo.length() > 255) {
            orderInfo = orderInfo.substring(0, 255);
        }
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        // Một số cấu hình VNPAY yêu cầu order type hợp lệ, ưu tiên "billpayment"
        vnp_Params.put("vnp_OrderType", "billpayment");
        vnp_Params.put("vnp_ReturnUrl", returnUrl);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        
        // Thêm ExpireDate (thời gian hết hạn - 15 phút)
        Calendar expireDate = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        expireDate.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(expireDate.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Sắp xếp và build chuỗi ký
        // Theo code demo VNPAY (ajaxServlet.java): encode fieldValue, KHÔNG encode fieldName khi tạo hash
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                if (hashData.length() > 0) {
                    hashData.append('&');
                }
                // Build hash data: fieldName (không encode) + "=" + fieldValue (ENCODE)
                // Theo code demo VNPAY (ajaxServlet.java dòng 88-90): encode fieldValue
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (java.io.UnsupportedEncodingException e) {
                    // US-ASCII luôn được hỗ trợ, nhưng vẫn cần handle exception
                    hashData.append(fieldValue);
                }

                // Query string vẫn encode cả fieldName và fieldValue cho URL
                if (query.length() > 0) {
                    query.append('&');
                }
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
            }
        }

        // Create secure hash
        String vnp_SecureHash = hmacSHA512(vnpHashSecret, hashData.toString());
        log.info("VNPAY request hashData: {}", hashData.toString());
        log.info("VNPAY request computed hash: {}", vnp_SecureHash);
        log.info("VNPAY request fieldNames: {}", fieldNames);
        if (query.length() > 0) {
            query.append('&');
        }
        // Khai báo loại hash theo tài liệu VNPAY
        query.append("vnp_SecureHashType=").append("HmacSHA512");
        query.append('&');
        query.append("vnp_SecureHash=").append(URLEncoder.encode(vnp_SecureHash, StandardCharsets.US_ASCII.toString()));
        String queryUrl = query.toString();

        String fullUrl = vnpPaymentUrl + "?" + queryUrl;
        log.info("VNPAY request (orderId={}): {}", orderId, fullUrl);
        log.debug("VNPAY hashData: {}", hashData);
        return fullUrl;
    }

    public PaymentResponse processVnPayReturn(Map<String, String> vnpParams) {
        log.info("VNPAY callback received params: {}", vnpParams);
        
        // Remove vnp_SecureHash to validate
        String vnp_SecureHash = vnpParams.get("vnp_SecureHash");
        if (vnp_SecureHash == null) {
            log.error("VNPAY callback missing vnp_SecureHash");
            return PaymentResponse.failure("Invalid signature");
        }

        // Remove hash and signature from params
        Map<String, String> signParams = new HashMap<>(vnpParams);
        signParams.remove("vnp_SecureHash");
        signParams.remove("vnp_SecureHashType");
        
        log.debug("VNPAY signParams (after removing hash): {}", signParams);

        // Build hash data from response
        // Theo code demo VNPay (vnpay_return.jsp và vnpay_ipn.jsp):
        // - Khi tạo request: fieldName (không encode) + "=" + fieldValue (encode)
        // - Khi xử lý return: ENCODE cả fieldName và fieldValue, rồi put vào Map
        // - Sau đó sort keys và build hashData từ Map đã encode
        // Lưu ý: Spring đã decode các giá trị từ URL, nên cần encode lại cả fieldName và fieldValue
        Map<String, String> encodedFields = new HashMap<>();
        for (Map.Entry<String, String> entry : signParams.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                try {
                    // Encode cả fieldName và fieldValue (theo code demo VNPay vnpay_return.jsp dòng 36-37)
                    String encodedFieldName = URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString());
                    String encodedFieldValue = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString());
                    encodedFields.put(encodedFieldName, encodedFieldValue);
                } catch (java.io.UnsupportedEncodingException e) {
                    // US-ASCII luôn được hỗ trợ, nhưng vẫn cần handle exception
                    encodedFields.put(fieldName, fieldValue);
                }
            }
        }
        
        // Sắp xếp và build hash data từ các field đã encode (theo Config.hashAllFields)
        List<String> fieldNames = new ArrayList<>(encodedFields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = encodedFields.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                if (hashData.length() > 0) {
                    hashData.append('&');
                }
                // Dùng fieldName và fieldValue đã được encode (theo Config.hashAllFields dòng 76-78)
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(fieldValue);
            }
        }

        // Verify checksum
        String secureHash = hmacSHA512(vnpHashSecret, hashData.toString());
        log.info("VNPAY callback hashData: {}", hashData.toString());
        log.info("VNPAY callback computed hash: {}, received hash: {}", secureHash, vnp_SecureHash);
        
        if (!secureHash.equals(vnp_SecureHash)) {
            log.error("VNPAY signature mismatch! Computed: {}, Received: {}, HashData: {}", 
                    secureHash, vnp_SecureHash, hashData.toString());
            log.error("VNPAY hashSecret length: {}", vnpHashSecret != null ? vnpHashSecret.length() : 0);
            return PaymentResponse.failure("Invalid signature");
        }
        
        log.info("VNPAY signature verification successful");

        // Get payment result
        // VNPAY có thể trả về vnp_ResponseCode hoặc vnp_TransactionStatus
        String vnp_ResponseCode = vnpParams.get("vnp_ResponseCode");
        String vnp_TransactionStatus = vnpParams.get("vnp_TransactionStatus");
        
        // Extract orderId từ vnp_TxnRef
        Integer orderId = null;
        try {
            String vnp_TxnRef = vnpParams.get("vnp_TxnRef");
            if (vnp_TxnRef != null && !vnp_TxnRef.isEmpty()) {
                String orderIdStr = vnp_TxnRef;
                if (vnp_TxnRef.contains("_")) {
                    orderIdStr = vnp_TxnRef.split("_")[0];
                }
                orderId = Integer.parseInt(orderIdStr);
            }
        } catch (Exception e) {
            // Nếu không extract được orderId, vẫn tiếp tục xử lý
            log.warn("Không thể extract orderId từ vnp_TxnRef: {}", vnpParams.get("vnp_TxnRef"));
        }
        
        // Kiểm tra thành công: vnp_ResponseCode = "00" HOẶC vnp_TransactionStatus = "00"
        boolean isSuccess = false;
        String errorMessage = null;
        
        if (vnp_ResponseCode != null) {
            // Nếu có vnp_ResponseCode, kiểm tra nó
            if ("00".equals(vnp_ResponseCode)) {
                isSuccess = true;
            } else {
                errorMessage = "Payment failed with ResponseCode: " + vnp_ResponseCode;
            }
        } else if (vnp_TransactionStatus != null) {
            // Nếu không có vnp_ResponseCode, kiểm tra vnp_TransactionStatus
            if ("00".equals(vnp_TransactionStatus)) {
                isSuccess = true;
            } else {
                // Mã lỗi TransactionStatus
                String statusMessage = getTransactionStatusMessage(vnp_TransactionStatus);
                errorMessage = "Payment failed. TransactionStatus: " + vnp_TransactionStatus + " - " + statusMessage;
            }
        } else {
            // Không có cả hai trường
            errorMessage = "Invalid payment response: missing both vnp_ResponseCode and vnp_TransactionStatus";
        }
        
        if (isSuccess) {
            // Payment successful
            String vnp_TransactionNo = vnpParams.get("vnp_TransactionNo");
            String transactionInfo = "VNPAY Transaction ID: " + (vnp_TransactionNo != null ? vnp_TransactionNo : "N/A");
            return PaymentResponse.success(orderId, transactionInfo, vnp_TransactionNo);
        } else {
            // Payment failed - vẫn trả về orderId nếu có
            return PaymentResponse.failure(orderId, errorMessage != null ? errorMessage : "Payment failed");
        }
    }

    // HMAC-SHA512 encryption
    private String hmacSHA512(String key, String data) {
        try {
            Mac sha512Hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            sha512Hmac.init(secretKeySpec);
            byte[] hmacData = sha512Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hmacData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC-SHA512", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    // Lấy thông báo lỗi từ TransactionStatus
    private String getTransactionStatusMessage(String status) {
        switch (status) {
            case "00":
                return "Giao dịch thành công";
            case "01":
                return "Giao dịch chưa hoàn tất";
            case "02":
                return "Giao dịch bị lỗi";
            case "04":
                return "Giao dịch đảo (Khách hàng đã bị trừ tiền nhưng GD chưa thành công do có lỗi xảy ra)";
            case "05":
                return "VNPAY đang xử lý giao dịch này (GD hoàn tiền)";
            case "06":
                return "VNPAY đã gửi yêu cầu hoàn tiền sang Ngân hàng (GD hoàn tiền)";
            case "07":
                return "Giao dịch bị nghi ngờ gian lận";
            case "09":
                return "GD Hoàn trả bị từ chối";
            default:
                return "Trạng thái không xác định";
        }
    }
}
