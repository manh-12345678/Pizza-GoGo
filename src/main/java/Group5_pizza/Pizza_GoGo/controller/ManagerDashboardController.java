package Group5_pizza.Pizza_GoGo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import Group5_pizza.Pizza_GoGo.DTO.DashboardRecentOrderDTO;
import Group5_pizza.Pizza_GoGo.DTO.DashboardRecentPaymentDTO;
import Group5_pizza.Pizza_GoGo.DTO.DashboardSummaryDTO;
import Group5_pizza.Pizza_GoGo.service.DashboardService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerDashboardController {

    private final DashboardService dashboardService;

    @GetMapping({"", "/", "/dashboard"})
    public String viewDashboard(Model model, @RequestParam(defaultValue = "5") int limit) {
        try {
            int normalizedLimit = Math.max(1, Math.min(limit, 10));

            DashboardSummaryDTO summary = dashboardService.getSummary();
            Map<String, Long> orderStatusDistribution = dashboardService.getOrderStatusDistribution();
            Map<String, Long> paymentStatusDistribution = dashboardService.getPaymentStatusDistribution();
            Map<String, Long> tableStatusDistribution = dashboardService.getTableStatusDistribution();
            Map<String, Long> ordersLast7Days = dashboardService.getOrdersLast7Days();

            List<DashboardRecentOrderDTO> recentOrders = dashboardService.getRecentOrders(normalizedLimit);
            List<DashboardRecentPaymentDTO> recentPayments = dashboardService.getRecentPayments(normalizedLimit);

            model.addAttribute("summary", summary);
            model.addAttribute("orderStatusDistribution", orderStatusDistribution);
            model.addAttribute("paymentStatusDistribution", paymentStatusDistribution);
            model.addAttribute("tableStatusDistribution", tableStatusDistribution);
            model.addAttribute("ordersLast7Days", ordersLast7Days);
            model.addAttribute("recentOrders", recentOrders);
            model.addAttribute("recentPayments", recentPayments);
            model.addAttribute("activePage", "dashboard");

            return "manager/dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra khi tải dashboard: " + e.getMessage());
            return "manager/dashboard";
        }
    }
}

