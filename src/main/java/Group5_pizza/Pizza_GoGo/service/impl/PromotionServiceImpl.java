package Group5_pizza.Pizza_GoGo.service.impl;

import Group5_pizza.Pizza_GoGo.model.Customer;
import Group5_pizza.Pizza_GoGo.model.Promotion;
import Group5_pizza.Pizza_GoGo.model.PromotionRedemption;
import Group5_pizza.Pizza_GoGo.repository.CustomerRepository;
import Group5_pizza.Pizza_GoGo.repository.PromotionRedemptionRepository;
import Group5_pizza.Pizza_GoGo.repository.PromotionRepository;
import Group5_pizza.Pizza_GoGo.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionRedemptionRepository redemptionRepository;
    private final CustomerRepository customerRepository;

    @Override
    public List<Promotion> getAllPromotions() {
        return promotionRepository.findByIsDeletedFalse();
    }

    @Override
    public List<Promotion> getAvailablePromotions() {
        return promotionRepository.findAvailablePromotions(LocalDateTime.now());
    }

    @Override
    public Promotion getPromotionById(Integer promotionId) {
        return promotionRepository.findByPromotionIdAndIsDeletedFalse(promotionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy promotion với ID: " + promotionId));
    }

    @Override
    @Transactional
    public Promotion createPromotion(Promotion promotion) {
        if (promotion == null) {
            throw new IllegalArgumentException("Promotion cannot be null");
        }
        return promotionRepository.save(promotion);
    }

    @Override
    @Transactional
    public Promotion updatePromotion(Integer promotionId, Promotion promotion) {
        Promotion existing = getPromotionById(promotionId);
        existing.setName(promotion.getName());
        existing.setDescription(promotion.getDescription());
        existing.setType(promotion.getType());
        existing.setPointsRequired(promotion.getPointsRequired());
        existing.setVoucherId(promotion.getVoucherId());
        existing.setGiftName(promotion.getGiftName());
        existing.setGiftDescription(promotion.getGiftDescription());
        existing.setProductId(promotion.getProductId());
        existing.setProductQuantity(promotion.getProductQuantity());
        existing.setImageUrl(promotion.getImageUrl());
        existing.setIsActive(promotion.getIsActive());
        existing.setStockQuantity(promotion.getStockQuantity());
        existing.setStartDate(promotion.getStartDate());
        existing.setEndDate(promotion.getEndDate());
        return promotionRepository.save(existing);
    }

    @Override
    @Transactional
    public boolean deletePromotion(Integer promotionId) {
        Promotion promotion = getPromotionById(promotionId);
        promotion.setIsDeleted(true);
        promotionRepository.save(promotion);
        return true;
    }

    @Override
    @Transactional
    public PromotionRedemption redeemPromotion(Integer customerId, Integer promotionId, String notes) {
        Customer customer = getCustomerById(customerId);
        Promotion promotion = getPromotionById(promotionId);

        // Kiểm tra promotion có sẵn không
        if (!promotion.isAvailable()) {
            throw new RuntimeException("Promotion không khả dụng");
        }

        // Kiểm tra điểm
        if (!hasEnoughPoints(customer, promotion.getPointsRequired())) {
            throw new RuntimeException("Không đủ điểm để đổi promotion này");
        }

        // Kiểm tra stock
        if (promotion.getStockQuantity() != null && promotion.getStockQuantity() <= 0) {
            throw new RuntimeException("Promotion đã hết hàng");
        }

        // Tạo redemption
        PromotionRedemption redemption = PromotionRedemption.builder()
                .customer(customer)
                .promotion(promotion)
                .pointsUsed(promotion.getPointsRequired())
                .status("PENDING")
                .notes(notes)
                .build();

        // Xử lý theo type
        if ("VOUCHER".equals(promotion.getType())) {
            // Tạo mã voucher code
            String voucherCode = generateVoucherCode();
            redemption.setVoucherCode(voucherCode);
            redemption.setStatus("COMPLETED");
            redemption.setCompletedAt(LocalDateTime.now());
        } else if ("GIFT".equals(promotion.getType())) {
            // GIFT cần staff xác nhận
            redemption.setStatus("PENDING");
        } else if ("PRODUCT".equals(promotion.getType())) {
            // PRODUCT: Tặng món - tự động hoàn thành, sản phẩm sẽ được thêm vào đơn hàng tiếp theo
            redemption.setStatus("COMPLETED");
            redemption.setCompletedAt(LocalDateTime.now());
            // Lưu productId và quantity vào notes để dùng sau
            if (promotion.getProductId() != null) {
                redemption.setNotes("PRODUCT:" + promotion.getProductId() + ":" + 
                    (promotion.getProductQuantity() != null ? promotion.getProductQuantity() : 1));
            }
        }

        // Trừ điểm
        Integer currentPoints = customer.getPoints() != null ? customer.getPoints() : 0;
        customer.setPoints(currentPoints - promotion.getPointsRequired());
        customerRepository.save(customer);

        // Giảm stock nếu có
        if (promotion.getStockQuantity() != null) {
            promotion.setStockQuantity(promotion.getStockQuantity() - 1);
            promotionRepository.save(promotion);
        }

        PromotionRedemption saved = redemptionRepository.save(redemption);
        log.info("Promotion redeemed: customerId={}, promotionId={}, status={}, voucherCode={}", 
                customerId, promotionId, saved.getStatus(), saved.getVoucherCode());
        return saved;
    }

    @Override
    public List<PromotionRedemption> getCustomerRedemptions(Integer customerId) {
        Customer customer = getCustomerById(customerId);
        return redemptionRepository.findByCustomerOrderByCreatedAtDesc(customer);
    }

    @Override
    public List<PromotionRedemption> getAllRedemptions() {
        return redemptionRepository.findAll();
    }

    @Override
    @Transactional
    public boolean completeRedemption(Integer redemptionId) {
        if (redemptionId == null) {
            throw new IllegalArgumentException("Redemption ID cannot be null");
        }
        PromotionRedemption redemption = redemptionRepository.findById(redemptionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy redemption với ID: " + redemptionId));

        if ("COMPLETED".equals(redemption.getStatus())) {
            return true; // Đã hoàn thành rồi
        }

        redemption.setStatus("COMPLETED");
        redemption.setCompletedAt(LocalDateTime.now());
        redemptionRepository.save(redemption);
        return true;
    }

    @Override
    @Transactional
    public boolean cancelRedemption(Integer redemptionId) {
        if (redemptionId == null) {
            throw new IllegalArgumentException("Redemption ID cannot be null");
        }
        PromotionRedemption redemption = redemptionRepository.findById(redemptionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy redemption với ID: " + redemptionId));

        if ("CANCELLED".equals(redemption.getStatus())) {
            return true;
        }

        // Hoàn lại điểm
        Customer customer = redemption.getCustomer();
        Integer currentPoints = customer.getPoints() != null ? customer.getPoints() : 0;
        customer.setPoints(currentPoints + redemption.getPointsUsed());
        customerRepository.save(customer);

        // Hoàn lại stock
        Promotion promotion = redemption.getPromotion();
        if (promotion.getStockQuantity() != null) {
            promotion.setStockQuantity(promotion.getStockQuantity() + 1);
            promotionRepository.save(promotion);
        }

        redemption.setStatus("CANCELLED");
        redemptionRepository.save(redemption);
        return true;
    }

    @Override
    public Customer getCustomerById(Integer customerId) {
        return customerRepository.findActiveCustomerById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy customer với ID: " + customerId));
    }

    @Override
    public boolean hasEnoughPoints(Customer customer, Integer pointsRequired) {
        if (customer == null || pointsRequired == null) {
            return false;
        }
        Integer customerPoints = customer.getPoints() != null ? customer.getPoints() : 0;
        return customerPoints >= pointsRequired;
    }

    private String generateVoucherCode() {
        return "PROMO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

