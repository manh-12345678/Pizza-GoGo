package Group5_pizza.Pizza_GoGo.controller;

import java.io.File;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import Group5_pizza.Pizza_GoGo.model.RestaurantTable;
import Group5_pizza.Pizza_GoGo.service.RestaurantTableService;
import Group5_pizza.Pizza_GoGo.util.QRCodeGenerator;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping({"/tables","/manager/tables"})
public class RestaurantTableController {

    private final RestaurantTableService restaurantTableService;

    public RestaurantTableController(RestaurantTableService restaurantTableService) {
        this.restaurantTableService = restaurantTableService;
    }

    @GetMapping({"", "/list"})
    public String getAllTables(Model model) {
        List<RestaurantTable> tables = restaurantTableService.getAllTables();
        model.addAttribute("tables", tables);
        model.addAttribute("activePage", "tables");
        return "tables/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("table", new RestaurantTable());
        return "tables/form";
    }

    @PostMapping({"", "/"})
    public String createTable(@ModelAttribute RestaurantTable table,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        try {
            // 1. Lưu bàn trước để có tableId
            RestaurantTable savedTable = restaurantTableService.saveTable(table, null);

            // 2. Tạo URL cho QR
            String baseUrl = request.getScheme() + "://" + request.getServerName()
                    + ":" + request.getServerPort() + request.getContextPath();
            String orderUrl = baseUrl + "/orders/table/" + savedTable.getTableId();

            // 3. Sinh file QR trong static/qr_codes/
            String qrDir = "src/main/resources/static/qr_codes/";
            File dir = new File(qrDir);
            if (!dir.exists())
                dir.mkdirs();

            String qrFileName = "table_" + savedTable.getTableId() + ".png";
            String qrFilePath = qrDir + qrFileName;

            QRCodeGenerator.generateQRCodeImage(orderUrl, qrFilePath, 200, 200);

            // 4. Lưu đường dẫn QR vào DB
            savedTable.setQrCodeUrl("/qr_codes/" + qrFileName);
            restaurantTableService.saveTable(savedTable, null);

            redirectAttributes.addFlashAttribute("success",
                    "Bàn #" + savedTable.getTableNumber() + " tạo thành công! QR code đã sẵn sàng.");
            return "redirect:/manager/tables";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Tạo bàn thất bại: " + e.getMessage());
            return "redirect:/manager/tables/new";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        RestaurantTable table = restaurantTableService.getTableById(id);
        model.addAttribute("table", table);
        return "tables/form";
    }

    @PostMapping("/{id}")
    public String updateTable(@PathVariable Integer id,
            @ModelAttribute RestaurantTable table,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        try {
            table.setTableId(id);
            RestaurantTable savedTable = restaurantTableService.saveTable(table, null);

            // Nếu chưa có QR code thì tạo mới
            if (savedTable.getQrCodeUrl() == null || savedTable.getQrCodeUrl().isEmpty()) {
                String baseUrl = request.getScheme() + "://" + request.getServerName()
                        + ":" + request.getServerPort() + request.getContextPath();
                String orderUrl = baseUrl + "/orders/table/" + savedTable.getTableId();

                String qrDir = "src/main/resources/static/qr_codes/";
                File dir = new File(qrDir);
                if (!dir.exists())
                    dir.mkdirs();

                String qrFileName = "table_" + savedTable.getTableId() + ".png";
                String qrFilePath = qrDir + qrFileName;

                QRCodeGenerator.generateQRCodeImage(orderUrl, qrFilePath, 200, 200);

                savedTable.setQrCodeUrl("/qr_codes/" + qrFileName);
                restaurantTableService.saveTable(savedTable, null);
            }

            redirectAttributes.addFlashAttribute("success", "Bàn cập nhật thành công!");
            return "redirect:/manager/tables";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cập nhật bàn thất bại: " + e.getMessage());
            return "redirect:/manager/tables/edit/" + id;
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteTable(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            RestaurantTable table = restaurantTableService.getTableById(id);

            // Xóa file QR nếu có
            if (table.getQrCodeUrl() != null) {
                File qrFile = new File("src/main/resources/static" + table.getQrCodeUrl());
                if (qrFile.exists())
                    qrFile.delete();
            }

            restaurantTableService.deleteTable(id);
            redirectAttributes.addFlashAttribute("success", "Bàn đã xóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Xóa bàn thất bại: " + e.getMessage());
        }
        return "redirect:/manager/tables";
    }

    @GetMapping("/view/{id}")
    public String getTableById(@PathVariable Integer id, Model model) {
        try {
            RestaurantTable table = restaurantTableService.getTableById(id);
            model.addAttribute("table", table);
            return "tables/view";
        } catch (Exception e) {
            return "redirect:/manager/tables";
        }
    }
}
