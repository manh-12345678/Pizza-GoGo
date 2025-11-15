package Group5_pizza.Pizza_GoGo.controller;

import Group5_pizza.Pizza_GoGo.DTO.ComboDTO;
import Group5_pizza.Pizza_GoGo.model.Combo;
import Group5_pizza.Pizza_GoGo.model.Product;
import Group5_pizza.Pizza_GoGo.service.ComboService;
import Group5_pizza.Pizza_GoGo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller xử lý Combo Management với đầy đủ CRUD operations
 */
@Controller
@RequestMapping({"/combos","/manager/combos"})
@RequiredArgsConstructor
public class ComboController {

    private final ComboService comboService;
    private final ProductService productService;

    // ========== WEB VIEWS (HTML) ==========

    /**
     * Hiển thị danh sách tất cả combo
     */
    @GetMapping("/manage")
    public String manageCombos(@RequestParam(required = false) String search, Model model) {
        try {
            List<Combo> combos;
            if (search != null && !search.trim().isEmpty()) {
                combos = comboService.searchCombosByName(search);
                model.addAttribute("search", search);
            } else {
                combos = comboService.getAllAvailableCombos();
            }
            model.addAttribute("combos", combos);
            return "combos/manage_combos";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi tải danh sách combo: " + e.getMessage());
            return "combos/manage_combos";
        }
    }

    /**
     * Hiển thị form tạo combo mới
     */
    @GetMapping("/add")
    public String showAddComboForm(Model model) {
        try {
            ComboDTO comboDTO = new ComboDTO();
            comboDTO.setComboDetails(new ArrayList<>());
            
            List<Product> availableProducts = productService.getAllProducts();
            
            model.addAttribute("comboDTO", comboDTO);
            model.addAttribute("products", availableProducts);
            return "combos/add_combo_form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi tải form: " + e.getMessage());
            return "redirect:/manager/combos/manage";
        }
    }

    /**
     * Xử lý tạo combo mới
     */
    @PostMapping("/add")
    public String addCombo(@ModelAttribute("comboDTO") ComboDTO comboDTO,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        try {
            // Validate input
            if (comboDTO.getName() == null || comboDTO.getName().trim().isEmpty()) {
                model.addAttribute("errorMessage", "Tên combo không được để trống");
                model.addAttribute("comboDTO", comboDTO);
                model.addAttribute("products", productService.getAllProducts());
                return "combos/add_combo_form";
            }

            if (comboDTO.getDiscountPercent() == null || 
                comboDTO.getDiscountPercent().compareTo(java.math.BigDecimal.ZERO) < 0 ||
                comboDTO.getDiscountPercent().compareTo(new java.math.BigDecimal("100")) > 0) {
                model.addAttribute("errorMessage", "Phần trăm giảm giá phải từ 0 đến 100");
                model.addAttribute("comboDTO", comboDTO);
                model.addAttribute("products", productService.getAllProducts());
                return "combos/add_combo_form";
            }

            comboService.createCombo(comboDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo combo thành công!");
            return "redirect:/manager/combos/manage";
            
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("comboDTO", comboDTO);
            model.addAttribute("products", productService.getAllProducts());
            return "combos/add_combo_form";
        } catch (Exception e) {
            System.err.println("Lỗi khi tạo combo: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi khi tạo combo: " + e.getMessage());
            model.addAttribute("comboDTO", comboDTO);
            model.addAttribute("products", productService.getAllProducts());
            return "combos/add_combo_form";
        }
    }

    /**
     * Hiển thị form chỉnh sửa combo
     */
    @GetMapping("/edit/{comboId}")
    public String showEditComboForm(@PathVariable Integer comboId, Model model) {
        try {
            Combo combo = comboService.getComboById(comboId);
            ComboDTO comboDTO = comboService.convertToDTO(combo);
            
            List<Product> availableProducts = productService.getAllProducts();
            
            model.addAttribute("comboDTO", comboDTO);
            model.addAttribute("products", availableProducts);
            model.addAttribute("isEdit", true);
            return "combos/edit_combo_form";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Không tìm thấy combo");
            return "redirect:/manager/combos/manage";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi tải thông tin combo: " + e.getMessage());
            return "redirect:/manager/combos/manage";
        }
    }

    /**
     * Xử lý cập nhật combo
     */
    @PostMapping("/edit/{comboId}")
    public String updateCombo(@PathVariable Integer comboId,
                             @ModelAttribute("comboDTO") ComboDTO comboDTO,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        try {
            // Validate input
            if (comboDTO.getName() == null || comboDTO.getName().trim().isEmpty()) {
                model.addAttribute("errorMessage", "Tên combo không được để trống");
                model.addAttribute("comboDTO", comboDTO);
                model.addAttribute("products", productService.getAllProducts());
                model.addAttribute("isEdit", true);
                return "combos/edit_combo_form";
            }

            if (comboDTO.getDiscountPercent() == null || 
                comboDTO.getDiscountPercent().compareTo(java.math.BigDecimal.ZERO) < 0 ||
                comboDTO.getDiscountPercent().compareTo(new java.math.BigDecimal("100")) > 0) {
                model.addAttribute("errorMessage", "Phần trăm giảm giá phải từ 0 đến 100");
                model.addAttribute("comboDTO", comboDTO);
                model.addAttribute("products", productService.getAllProducts());
                model.addAttribute("isEdit", true);
                return "combos/edit_combo_form";
            }

            comboService.updateCombo(comboId, comboDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật combo thành công!");
            return "redirect:/manager/combos/manage";
            
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("comboDTO", comboDTO);
            model.addAttribute("products", productService.getAllProducts());
            model.addAttribute("isEdit", true);
            return "combos/edit_combo_form";
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật combo: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi khi cập nhật combo: " + e.getMessage());
            model.addAttribute("comboDTO", comboDTO);
            model.addAttribute("products", productService.getAllProducts());
            model.addAttribute("isEdit", true);
            return "combos/edit_combo_form";
        }
    }

    /**
     * Hiển thị chi tiết combo
     */
    @GetMapping("/view/{comboId}")
    public String viewComboDetails(@PathVariable Integer comboId, Model model) {
        try {
            Combo combo = comboService.getComboById(comboId);
            ComboDTO comboDTO = comboService.convertToDTO(combo);
            
            model.addAttribute("combo", combo);
            model.addAttribute("comboDTO", comboDTO);
            return "combos/view_combo";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Không tìm thấy combo");
            return "redirect:/manager/combos/manage";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi tải thông tin combo: " + e.getMessage());
            return "redirect:/manager/combos/manage";
        }
    }

    /**
     * Xóa combo (soft delete)
     */
    @PostMapping("/delete/{comboId}")
    public String deleteCombo(@PathVariable Integer comboId, RedirectAttributes redirectAttributes) {
        try {
            boolean deleted = comboService.deleteCombo(comboId);
            if (deleted) {
                redirectAttributes.addFlashAttribute("successMessage", "Đã xóa combo thành công");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa combo");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa combo: " + e.getMessage());
        }
        return "redirect:/manager/combos/manage";
    }

    // ========== REST API ENDPOINTS ==========

    /**
     * API: Lấy danh sách tất cả combo khả dụng
     */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<List<ComboDTO>> getAllCombosApi() {
        try {
            List<Combo> combos = comboService.getAllAvailableCombos();
            List<ComboDTO> comboDTOs = combos.stream()
                .map(comboService::convertToDTO)
                .toList();
            return ResponseEntity.ok(comboDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * API: Lấy combo theo ID
     */
    @GetMapping("/api/{comboId}")
    @ResponseBody
    public ResponseEntity<ComboDTO> getComboByIdApi(@PathVariable Integer comboId) {
        try {
            Combo combo = comboService.getComboById(comboId);
            ComboDTO comboDTO = comboService.convertToDTO(combo);
            return ResponseEntity.ok(comboDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * API: Tạo combo mới
     */
    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createComboApi(@RequestBody ComboDTO comboDTO) {
        Map<String, Object> response = new HashMap<>();
        try {
            Combo createdCombo = comboService.createCombo(comboDTO);
            ComboDTO resultDTO = comboService.convertToDTO(createdCombo);
            
            response.put("success", true);
            response.put("message", "Tạo combo thành công");
            response.put("data", resultDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi tạo combo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API: Cập nhật combo
     */
    @PutMapping("/api/update/{comboId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateComboApi(@PathVariable Integer comboId, 
                                                               @RequestBody ComboDTO comboDTO) {
        Map<String, Object> response = new HashMap<>();
        try {
            Combo updatedCombo = comboService.updateCombo(comboId, comboDTO);
            ComboDTO resultDTO = comboService.convertToDTO(updatedCombo);
            
            response.put("success", true);
            response.put("message", "Cập nhật combo thành công");
            response.put("data", resultDTO);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", "Không tìm thấy combo");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi cập nhật combo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API: Xóa combo
     */
    @DeleteMapping("/api/delete/{comboId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteComboApi(@PathVariable Integer comboId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean deleted = comboService.deleteCombo(comboId);
            if (deleted) {
                response.put("success", true);
                response.put("message", "Đã xóa combo thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Không thể xóa combo");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", "Không tìm thấy combo");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi xóa combo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API: Tìm kiếm combo theo tên
     */
    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<List<ComboDTO>> searchCombosApi(@RequestParam String name) {
        try {
            List<Combo> combos = comboService.searchCombosByName(name);
            List<ComboDTO> comboDTOs = combos.stream()
                .map(comboService::convertToDTO)
                .toList();
            return ResponseEntity.ok(comboDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * API: Lấy danh sách combo đang hoạt động (trong khoảng thời gian)
     */
    @GetMapping("/api/active")
    @ResponseBody
    public ResponseEntity<List<ComboDTO>> getActiveCombosApi() {
        try {
            List<Combo> activeCombos = comboService.getActiveCombos();
            List<ComboDTO> comboDTOs = activeCombos.stream()
                .map(comboService::convertToDTO)
                .toList();
            return ResponseEntity.ok(comboDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
