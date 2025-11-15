package Group5_pizza.Pizza_GoGo.service;

import Group5_pizza.Pizza_GoGo.model.Customer;
import Group5_pizza.Pizza_GoGo.model.Promotion;
import Group5_pizza.Pizza_GoGo.model.PromotionRedemption;

import java.util.List;

public interface PromotionService {
    List<Promotion> getAllPromotions();
    
    List<Promotion> getAvailablePromotions();
    
    Promotion getPromotionById(Integer promotionId);
    
    Promotion createPromotion(Promotion promotion);
    
    Promotion updatePromotion(Integer promotionId, Promotion promotion);
    
    boolean deletePromotion(Integer promotionId);
    
    PromotionRedemption redeemPromotion(Integer customerId, Integer promotionId, String notes);
    
    List<PromotionRedemption> getCustomerRedemptions(Integer customerId);
    
    List<PromotionRedemption> getAllRedemptions();
    
    boolean completeRedemption(Integer redemptionId);
    
    boolean cancelRedemption(Integer redemptionId);
    
    Customer getCustomerById(Integer customerId);
    
    boolean hasEnoughPoints(Customer customer, Integer pointsRequired);
}

