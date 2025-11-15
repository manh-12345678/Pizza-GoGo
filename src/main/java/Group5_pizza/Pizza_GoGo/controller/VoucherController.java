package Group5_pizza.Pizza_GoGo.controller;

import Group5_pizza.Pizza_GoGo.DTO.VoucherDTO;
import Group5_pizza.Pizza_GoGo.model.Voucher;
import Group5_pizza.Pizza_GoGo.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller xử lý Voucher Management với đầy đủ CRUD operations
 */
@Controller
@RequestMapping({"/vouchers","/manager/vouchers"})
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    // ========== WEB VIEWS (HTML) ==========

    /**
     * Hiển thị danh sách tất cả voucher
     */
    @GetMapping("/manage")
    public String manageVouchers(@RequestParam(required = false) String search, Model model) {
        try {
            List<Voucher> vouchers;
            if (search != null && !search.trim().isEmpty()) {
                vouchers = voucherService.searchVouchersByCode(search);
                model.addAttribute("search", search);
            } else {
                vouchers = voucherService.getAllVouchers();
            }
            model.addAttribute("vouchers", vouchers);
            return "vouchers/manage_vouchers";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi tải danh sách voucher: " + e.getMessage());
            return "vouchers/manage_vouchers";
        }
    }

    /**
     * Hiển thị form tạo voucher mới
     */
    @GetMapping("/add")
    public String showAddVoucherForm(Model model) {
        try {
            VoucherDTO voucherDTO = new VoucherDTO();
            voucherDTO.setIsActive(true);
            model.addAttribute("voucherDTO", voucherDTO);
            model.addAttribute("isEdit", false);
            return "vouchers/voucher_form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi tải form: " + e.getMessage());
            return "redirect:/manager/vouchers/manage";
        }
    }

    /**
     * Xử lý tạo voucher mới
     */
    @PostMapping("/add")
    public String addVoucher(@ModelAttribute("voucherDTO") VoucherDTO voucherDTO,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        try {
            voucherService.createVoucher(voucherDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo voucher thành công!");
            return "redirect:/manager/vouchers/manage";
            
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("voucherDTO", voucherDTO);
            model.addAttribute("isEdit", false);
            return "vouchers/voucher_form";
        } catch (Exception e) {
            System.err.println("Lỗi khi tạo voucher: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi khi tạo voucher: " + e.getMessage());
            model.addAttribute("voucherDTO", voucherDTO);
            model.addAttribute("isEdit", false);
            return "vouchers/voucher_form";
        }
    }

    /**
     * Hiển thị form chỉnh sửa voucher
     */
    @GetMapping("/edit/{voucherId}")
    public String showEditVoucherForm(@PathVariable Integer voucherId, Model model) {
        try {
            Voucher voucher = voucherService.getVoucherById(voucherId);
            VoucherDTO voucherDTO = voucherService.convertToDTO(voucher);
            
            model.addAttribute("voucherDTO", voucherDTO);
            model.addAttribute("isEdit", true);
            return "vouchers/voucher_form";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Không tìm thấy voucher");
            return "redirect:/manager/vouchers/manage";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi tải thông tin voucher: " + e.getMessage());
            return "redirect:/manager/vouchers/manage";
        }
    }

    /**
     * Xử lý cập nhật voucher
     */
    @PostMapping("/edit/{voucherId}")
    public String updateVoucher(@PathVariable Integer voucherId,
                             @ModelAttribute("voucherDTO") VoucherDTO voucherDTO,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        try {
            voucherService.updateVoucher(voucherId, voucherDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật voucher thành công!");
            return "redirect:/manager/vouchers/manage";
            
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("voucherDTO", voucherDTO);
            model.addAttribute("isEdit", true);
            return "vouchers/voucher_form";
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật voucher: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi khi cập nhật voucher: " + e.getMessage());
            model.addAttribute("voucherDTO", voucherDTO);
            model.addAttribute("isEdit", true);
            return "vouchers/voucher_form";
        }
    }

    /**
     * Xóa voucher
     */
    @PostMapping("/delete/{voucherId}")
    public String deleteVoucher(@PathVariable Integer voucherId, RedirectAttributes redirectAttributes) {
        try {
            boolean deleted = voucherService.deleteVoucher(voucherId);
            if (deleted) {
                redirectAttributes.addFlashAttribute("successMessage", "Đã xóa voucher thành công");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa voucher");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa voucher: " + e.getMessage());
        }
        return "redirect:/vouchers/manage";
    }

    /**
     * Kích hoạt voucher
     */
    @PostMapping("/activate/{voucherId}")
    public String activateVoucher(@PathVariable Integer voucherId, RedirectAttributes redirectAttributes) {
        try {
            voucherService.activateVoucher(voucherId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã kích hoạt voucher thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi kích hoạt voucher: " + e.getMessage());
        }
        return "redirect:/vouchers/manage";
    }

    /**
     * Vô hiệu hóa voucher
     */
    @PostMapping("/deactivate/{voucherId}")
    public String deactivateVoucher(@PathVariable Integer voucherId, RedirectAttributes redirectAttributes) {
        try {
            voucherService.deactivateVoucher(voucherId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã vô hiệu hóa voucher thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi vô hiệu hóa voucher: " + e.getMessage());
        }
        return "redirect:/vouchers/manage";
    }

    // ========== REST API ENDPOINTS ==========

    /**
     * API: Lấy danh sách tất cả voucher
     */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<List<VoucherDTO>> getAllVouchersApi() {
        try {
            List<Voucher> vouchers = voucherService.getAllVouchers();
            List<VoucherDTO> voucherDTOs = vouchers.stream()
                .map(voucherService::convertToDTO)
                .toList();
            return ResponseEntity.ok(voucherDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * API: Lấy voucher theo ID
     */
    @GetMapping("/api/{voucherId}")
    @ResponseBody
    public ResponseEntity<VoucherDTO> getVoucherByIdApi(@PathVariable Integer voucherId) {
        try {
            Voucher voucher = voucherService.getVoucherById(voucherId);
            VoucherDTO voucherDTO = voucherService.convertToDTO(voucher);
            return ResponseEntity.ok(voucherDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * API: Lấy voucher theo mã code
     */
    @GetMapping("/api/code/{code}")
    @ResponseBody
    public ResponseEntity<VoucherDTO> getVoucherByCodeApi(@PathVariable String code) {
        try {
            Voucher voucher = voucherService.getVoucherByCode(code);
            VoucherDTO voucherDTO = voucherService.convertToDTO(voucher);
            return ResponseEntity.ok(voucherDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * API: Tạo voucher mới
     */
    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createVoucherApi(@RequestBody VoucherDTO voucherDTO) {
        Map<String, Object> response = new HashMap<>();
        try {
            Voucher createdVoucher = voucherService.createVoucher(voucherDTO);
            VoucherDTO resultDTO = voucherService.convertToDTO(createdVoucher);
            
            response.put("success", true);
            response.put("message", "Tạo voucher thành công");
            response.put("data", resultDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi tạo voucher: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API: Cập nhật voucher
     */
    @PutMapping("/api/update/{voucherId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateVoucherApi(@PathVariable Integer voucherId, 
                                                               @RequestBody VoucherDTO voucherDTO) {
        Map<String, Object> response = new HashMap<>();
        try {
            Voucher updatedVoucher = voucherService.updateVoucher(voucherId, voucherDTO);
            VoucherDTO resultDTO = voucherService.convertToDTO(updatedVoucher);
            
            response.put("success", true);
            response.put("message", "Cập nhật voucher thành công");
            response.put("data", resultDTO);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", "Không tìm thấy voucher");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi cập nhật voucher: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API: Xóa voucher
     */
    @DeleteMapping("/api/delete/{voucherId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteVoucherApi(@PathVariable Integer voucherId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean deleted = voucherService.deleteVoucher(voucherId);
            if (deleted) {
                response.put("success", true);
                response.put("message", "Đã xóa voucher thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Không thể xóa voucher");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", "Không tìm thấy voucher");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi xóa voucher: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API: Tìm kiếm voucher theo mã code
     */
    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<List<VoucherDTO>> searchVouchersApi(@RequestParam String code) {
        try {
            List<Voucher> vouchers = voucherService.searchVouchersByCode(code);
            List<VoucherDTO> voucherDTOs = vouchers.stream()
                .map(voucherService::convertToDTO)
                .toList();
            return ResponseEntity.ok(voucherDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * API: Lấy danh sách voucher đang hoạt động
     */
    @GetMapping("/api/active")
    @ResponseBody
    public ResponseEntity<List<VoucherDTO>> getActiveVouchersApi() {
        try {
            List<Voucher> activeVouchers = voucherService.getActiveVouchers();
            List<VoucherDTO> voucherDTOs = activeVouchers.stream()
                .map(voucherService::convertToDTO)
                .toList();
            return ResponseEntity.ok(voucherDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * API: Validate voucher code
     */
    @GetMapping("/api/validate/{code}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validateVoucherApi(@PathVariable String code) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean isValid = voucherService.validateVoucher(code);
            response.put("valid", isValid);
            if (isValid) {
                Voucher voucher = voucherService.getVoucherByCode(code);
                response.put("voucher", voucherService.convertToDTO(voucher));
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("valid", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}

