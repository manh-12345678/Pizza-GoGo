package Group5_pizza.Pizza_GoGo.controller;

import Group5_pizza.Pizza_GoGo.model.Shipping;
import Group5_pizza.Pizza_GoGo.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/manager/shipping")
@RequiredArgsConstructor
@Slf4j
public class ShippingController {

    private final ShippingService shippingService;

    @GetMapping
    public String listShippings(
            @RequestParam(value = "status", required = false) String status,
            Model model) {
        List<Shipping> shippings;
        if (status != null && !status.trim().isEmpty()) {
            shippings = shippingService.getShippingsByStatus(status);
        } else {
            shippings = shippingService.getAllShippings();
        }
        model.addAttribute("shippings", shippings);
        model.addAttribute("activePage", "shipping");
        model.addAttribute("selectedStatus", status);
        return "manager/shipping/list";
    }

    @GetMapping("/{shippingId}")
    public String viewShippingDetail(@PathVariable Integer shippingId, Model model) {
        Shipping shipping = shippingService.getShippingById(shippingId);
        model.addAttribute("shipping", shipping);
        model.addAttribute("activePage", "shipping");
        return "manager/shipping/detail";
    }

    @PostMapping("/{shippingId}/update-status")
    public String updateShippingStatus(
            @PathVariable Integer shippingId,
            @RequestParam("status") String status,
            @RequestParam(value = "shipperName", required = false) String shipperName,
            @RequestParam(value = "shipperPhone", required = false) String shipperPhone,
            RedirectAttributes redirectAttributes) {
        try {
            shippingService.updateShippingStatus(shippingId, status, shipperName, shipperPhone);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái giao hàng thành công");
        } catch (Exception e) {
            log.error("Error updating shipping status: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
        }
        return "redirect:/manager/shipping/" + shippingId;
    }

    @PostMapping("/{shippingId}/assign-shipper")
    public String assignShipper(
            @PathVariable Integer shippingId,
            @RequestParam("shipperName") String shipperName,
            @RequestParam("shipperPhone") String shipperPhone,
            RedirectAttributes redirectAttributes) {
        try {
            shippingService.assignShipper(shippingId, shipperName, shipperPhone);
            redirectAttributes.addFlashAttribute("success", "Phân công shipper thành công");
        } catch (Exception e) {
            log.error("Error assigning shipper: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi phân công shipper: " + e.getMessage());
        }
        return "redirect:/manager/shipping/" + shippingId;
    }

    @PostMapping("/{shippingId}/mark-delivered")
    public String markAsDelivered(
            @PathVariable Integer shippingId,
            RedirectAttributes redirectAttributes) {
        try {
            shippingService.markAsDelivered(shippingId);
            redirectAttributes.addFlashAttribute("success", "Đánh dấu đã giao hàng thành công");
        } catch (Exception e) {
            log.error("Error marking as delivered: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi đánh dấu đã giao: " + e.getMessage());
        }
        return "redirect:/manager/shipping/" + shippingId;
    }
}

