package Group5_pizza.Pizza_GoGo.controller;

import Group5_pizza.Pizza_GoGo.model.Promotion;
import Group5_pizza.Pizza_GoGo.model.PromotionRedemption;
import Group5_pizza.Pizza_GoGo.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/manager/promotions")
@RequiredArgsConstructor
@Slf4j
public class ManagerPromotionController {

    private final PromotionService promotionService;

    @GetMapping
    public String listPromotions(Model model) {
        List<Promotion> promotions = promotionService.getAllPromotions();
        model.addAttribute("promotions", promotions);
        model.addAttribute("activePage", "promotions");
        return "manager/promotions/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("activePage", "promotions");
        model.addAttribute("isEdit", false);
        model.addAttribute("promotion", null);
        return "manager/promotions/add";
    }

    @PostMapping("/add")
    public String addPromotion(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("type") String type,
            @RequestParam("pointsRequired") Integer pointsRequired,
            @RequestParam(value = "voucherId", required = false) Integer voucherId,
            @RequestParam(value = "giftName", required = false) String giftName,
            @RequestParam(value = "giftDescription", required = false) String giftDescription,
            @RequestParam(value = "productId", required = false) Integer productId,
            @RequestParam(value = "productQuantity", required = false) Integer productQuantity,
            @RequestParam(value = "stockQuantity", required = false) Integer stockQuantity,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr,
            @RequestParam(value = "imageUrl", required = false) String imageUrl,
            @RequestParam(value = "isActive", defaultValue = "false") Boolean isActive,
            RedirectAttributes redirectAttributes) {
        try {
            Promotion promotion = Promotion.builder()
                    .name(name)
                    .description(description)
                    .type(type)
                    .pointsRequired(pointsRequired)
                    .voucherId(voucherId)
                    .giftName(giftName)
                    .giftDescription(giftDescription)
                    .productId(productId)
                    .productQuantity(productQuantity != null ? productQuantity : 1)
                    .stockQuantity(stockQuantity)
                    .imageUrl(imageUrl)
                    .isActive(isActive)
                    .build();

            if (startDateStr != null && !startDateStr.trim().isEmpty()) {
                try {
                    // Format từ datetime-local: yyyy-MM-ddTHH:mm
                    promotion.setStartDate(LocalDateTime.parse(startDateStr.replace(" ", "T")));
                } catch (Exception e) {
                    log.warn("Error parsing startDate: {}", startDateStr, e);
                }
            }
            if (endDateStr != null && !endDateStr.trim().isEmpty()) {
                try {
                    // Format từ datetime-local: yyyy-MM-ddTHH:mm
                    promotion.setEndDate(LocalDateTime.parse(endDateStr.replace(" ", "T")));
                } catch (Exception e) {
                    log.warn("Error parsing endDate: {}", endDateStr, e);
                }
            }

            promotionService.createPromotion(promotion);
            redirectAttributes.addFlashAttribute("success", "Thêm promotion thành công");
        } catch (Exception e) {
            log.error("Error adding promotion: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/manager/promotions/add";
        }
        return "redirect:/manager/promotions";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        try {
            Promotion promotion = promotionService.getPromotionById(id);
            model.addAttribute("promotion", promotion);
            model.addAttribute("activePage", "promotions");
            model.addAttribute("isEdit", true);
            return "manager/promotions/add";
        } catch (Exception e) {
            log.error("Error loading promotion for edit: {}", e.getMessage(), e);
            return "redirect:/manager/promotions";
        }
    }

    @PostMapping("/edit/{id}")
    public String updatePromotion(
            @PathVariable Integer id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("type") String type,
            @RequestParam("pointsRequired") Integer pointsRequired,
            @RequestParam(value = "voucherId", required = false) Integer voucherId,
            @RequestParam(value = "giftName", required = false) String giftName,
            @RequestParam(value = "giftDescription", required = false) String giftDescription,
            @RequestParam(value = "productId", required = false) Integer productId,
            @RequestParam(value = "productQuantity", required = false) Integer productQuantity,
            @RequestParam(value = "stockQuantity", required = false) Integer stockQuantity,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr,
            @RequestParam(value = "imageUrl", required = false) String imageUrl,
            @RequestParam(value = "isActive", defaultValue = "false") Boolean isActive,
            RedirectAttributes redirectAttributes) {
        try {
            Promotion existing = promotionService.getPromotionById(id);
            
            // Cập nhật thông tin
            existing.setName(name);
            existing.setDescription(description);
            existing.setType(type);
            existing.setPointsRequired(pointsRequired);
            existing.setVoucherId(voucherId);
            existing.setGiftName(giftName);
            existing.setGiftDescription(giftDescription);
            existing.setProductId(productId);
            existing.setProductQuantity(productQuantity != null ? productQuantity : 1);
            existing.setStockQuantity(stockQuantity);
            existing.setImageUrl(imageUrl);
            existing.setIsActive(isActive);

            if (startDateStr != null && !startDateStr.trim().isEmpty()) {
                try {
                    // Format từ datetime-local: yyyy-MM-ddTHH:mm
                    existing.setStartDate(LocalDateTime.parse(startDateStr.replace(" ", "T")));
                } catch (Exception e) {
                    log.warn("Error parsing startDate: {}", startDateStr, e);
                }
            } else {
                existing.setStartDate(null);
            }
            if (endDateStr != null && !endDateStr.trim().isEmpty()) {
                try {
                    // Format từ datetime-local: yyyy-MM-ddTHH:mm
                    existing.setEndDate(LocalDateTime.parse(endDateStr.replace(" ", "T")));
                } catch (Exception e) {
                    log.warn("Error parsing endDate: {}", endDateStr, e);
                }
            } else {
                existing.setEndDate(null);
            }

            promotionService.updatePromotion(id, existing);
            redirectAttributes.addFlashAttribute("success", "Cập nhật promotion thành công");
        } catch (Exception e) {
            log.error("Error updating promotion: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/manager/promotions/edit/" + id;
        }
        return "redirect:/manager/promotions";
    }

    @PostMapping("/delete/{id}")
    public String deletePromotion(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {
        try {
            promotionService.deletePromotion(id);
            redirectAttributes.addFlashAttribute("success", "Xóa promotion thành công");
        } catch (Exception e) {
            log.error("Error deleting promotion: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/manager/promotions";
    }

    @GetMapping("/redemptions")
    public String listRedemptions(Model model) {
        List<PromotionRedemption> redemptions = promotionService.getAllRedemptions();
        model.addAttribute("redemptions", redemptions);
        model.addAttribute("activePage", "promotions");
        return "manager/promotions/redemptions";
    }

    @PostMapping("/redemptions/{redemptionId}/complete")
    public String completeRedemption(
            @PathVariable Integer redemptionId,
            RedirectAttributes redirectAttributes) {
        try {
            promotionService.completeRedemption(redemptionId);
            redirectAttributes.addFlashAttribute("success", "Xác nhận đổi promotion thành công");
        } catch (Exception e) {
            log.error("Error completing redemption: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/manager/promotions/redemptions";
    }

    @PostMapping("/redemptions/{redemptionId}/cancel")
    public String cancelRedemption(
            @PathVariable Integer redemptionId,
            RedirectAttributes redirectAttributes) {
        try {
            promotionService.cancelRedemption(redemptionId);
            redirectAttributes.addFlashAttribute("success", "Hủy đổi promotion thành công");
        } catch (Exception e) {
            log.error("Error cancelling redemption: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/manager/promotions/redemptions";
    }
}

