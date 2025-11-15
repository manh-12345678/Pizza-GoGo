package Group5_pizza.Pizza_GoGo.service.impl;

import Group5_pizza.Pizza_GoGo.DTO.ComboDTO;
import Group5_pizza.Pizza_GoGo.DTO.ComboDetailDTO;
import Group5_pizza.Pizza_GoGo.model.Combo;
import Group5_pizza.Pizza_GoGo.model.ComboDetail;
import Group5_pizza.Pizza_GoGo.model.Product;
import Group5_pizza.Pizza_GoGo.repository.ComboDetailRepository;
import Group5_pizza.Pizza_GoGo.repository.ComboRepository;
import Group5_pizza.Pizza_GoGo.repository.ProductRepository;
import Group5_pizza.Pizza_GoGo.service.ComboService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComboServiceImpl implements ComboService {

    private final ComboRepository comboRepository;
    private final ComboDetailRepository comboDetailRepository;
    private final ProductRepository productRepository;

    @Override
    public List<Combo> getAllCombos() {
        return comboRepository.findAll();
    }

    @Override
    public List<Combo> getAllAvailableCombos() {
        return comboRepository.findByIsDeletedFalseOrIsDeletedNull();
    }

    @Override
    public Combo getComboById(Integer comboId) {
        if (comboId == null) {
            throw new IllegalArgumentException("Combo ID không được để trống");
        }
        return comboRepository.findById(comboId)
            .orElseThrow(() -> new RuntimeException("Combo không tồn tại với ID: " + comboId));
    }

    @Override
    @Transactional
    public Combo createCombo(ComboDTO comboDTO) {
        validateComboDTO(comboDTO);
        
        Combo combo = new Combo();
        combo.setName(comboDTO.getName());
        combo.setDiscountPercent(comboDTO.getDiscountPercent());
        combo.setStartDate(comboDTO.getStartDate());
        combo.setEndDate(comboDTO.getEndDate());
        combo.setCreatedAt(LocalDateTime.now());
        combo.setUpdatedAt(LocalDateTime.now());
        combo.setIsDeleted(false);
        
        Combo savedCombo = comboRepository.save(combo);
        
        // Create combo details if provided
        if (comboDTO.getComboDetails() != null && !comboDTO.getComboDetails().isEmpty()) {
            List<ComboDetail> comboDetails = new ArrayList<>();
            for (ComboDetailDTO detailDTO : comboDTO.getComboDetails()) {
                ComboDetail detail = new ComboDetail();
                detail.setCombo(savedCombo);
                
                Integer productId = detailDTO.getProductId();
                if (productId == null) {
                    throw new IllegalArgumentException("Product ID không được để trống");
                }
                Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại với ID: " + productId));
                detail.setProduct(product);
                detail.setQuantity(detailDTO.getQuantity() != null ? detailDTO.getQuantity() : 1);
                
                comboDetails.add(detail);
            }
            comboDetailRepository.saveAll(comboDetails);
            savedCombo.setComboDetails(comboDetails);
        }
        
        return savedCombo;
    }

    @Override
    @Transactional
    public Combo updateCombo(Integer comboId, ComboDTO comboDTO) {
        validateComboDTO(comboDTO);
        
        Combo existingCombo = getComboById(comboId);
        
        existingCombo.setName(comboDTO.getName());
        existingCombo.setDiscountPercent(comboDTO.getDiscountPercent());
        existingCombo.setStartDate(comboDTO.getStartDate());
        existingCombo.setEndDate(comboDTO.getEndDate());
        existingCombo.setUpdatedAt(LocalDateTime.now());
        
        // Update combo details
        if (comboDTO.getComboDetails() != null) {
            // Delete existing details
            comboDetailRepository.deleteByCombo_ComboId(comboId);
            
            // Add new details
            List<ComboDetail> newDetails = new ArrayList<>();
            for (ComboDetailDTO detailDTO : comboDTO.getComboDetails()) {
                if (detailDTO.getProductId() != null && detailDTO.getQuantity() != null && detailDTO.getQuantity() > 0) {
                    ComboDetail detail = new ComboDetail();
                    detail.setCombo(existingCombo);
                    
                    Integer productId = detailDTO.getProductId();
                    if (productId == null) {
                        continue; // Skip null IDs
                    }
                    Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại với ID: " + productId));
                    detail.setProduct(product);
                    detail.setQuantity(detailDTO.getQuantity());
                    
                    newDetails.add(detail);
                }
            }
            comboDetailRepository.saveAll(newDetails);
            existingCombo.setComboDetails(newDetails);
        }
        
        return comboRepository.save(existingCombo);
    }

    @Override
    @Transactional
    public boolean deleteCombo(Integer comboId) {
        Combo combo = getComboById(comboId);
        combo.setIsDeleted(true);
        combo.setUpdatedAt(LocalDateTime.now());
        comboRepository.save(combo);
        return true;
    }

    @Override
    public List<Combo> searchCombosByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllAvailableCombos();
        }
        return comboRepository.findByNameContainingIgnoreCaseAndIsDeletedFalse(name);
    }

    @Override
    public List<Combo> getActiveCombos() {
        LocalDateTime now = LocalDateTime.now();
        return comboRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndIsDeletedFalse(now, now);
    }

    @Override
    public ComboDTO convertToDTO(Combo combo) {
        if (combo == null) {
            return null;
        }
        
        ComboDTO dto = new ComboDTO();
        dto.setComboId(combo.getComboId());
        dto.setName(combo.getName());
        dto.setDiscountPercent(combo.getDiscountPercent());
        dto.setStartDate(combo.getStartDate());
        dto.setEndDate(combo.getEndDate());
        dto.setCreatedAt(combo.getCreatedAt());
        dto.setUpdatedAt(combo.getUpdatedAt());
        dto.setIsDeleted(combo.getIsDeleted());
        
        if (combo.getComboDetails() != null) {
            List<ComboDetailDTO> detailDTOs = combo.getComboDetails().stream()
                .map(detail -> {
                    ComboDetailDTO detailDTO = new ComboDetailDTO();
                    detailDTO.setComboDetailId(detail.getComboDetailId());
                    detailDTO.setComboId(combo.getComboId());
                    detailDTO.setProductId(detail.getProduct().getProductId());
                    detailDTO.setProductName(detail.getProduct().getName());
                    detailDTO.setQuantity(detail.getQuantity());
                    return detailDTO;
                })
                .collect(Collectors.toList());
            dto.setComboDetails(detailDTOs);
        }
        
        return dto;
    }

    @Override
    public Combo convertToEntity(ComboDTO comboDTO) {
        if (comboDTO == null) {
            return null;
        }
        
        Combo combo = new Combo();
        combo.setComboId(comboDTO.getComboId());
        combo.setName(comboDTO.getName());
        combo.setDiscountPercent(comboDTO.getDiscountPercent());
        combo.setStartDate(comboDTO.getStartDate());
        combo.setEndDate(comboDTO.getEndDate());
        combo.setCreatedAt(comboDTO.getCreatedAt());
        combo.setUpdatedAt(comboDTO.getUpdatedAt());
        combo.setIsDeleted(comboDTO.getIsDeleted());
        
        return combo;
    }

    private void validateComboDTO(ComboDTO comboDTO) {
        if (comboDTO.getName() == null || comboDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên combo không được để trống");
        }
        
        if (comboDTO.getName().length() > 150) {
            throw new IllegalArgumentException("Tên combo không được vượt quá 150 ký tự");
        }
        
        if (comboDTO.getDiscountPercent() == null || comboDTO.getDiscountPercent().compareTo(java.math.BigDecimal.ZERO) < 0 
            || comboDTO.getDiscountPercent().compareTo(new java.math.BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Phần trăm giảm giá phải từ 0 đến 100");
        }
        
        if (comboDTO.getStartDate() != null && comboDTO.getEndDate() != null 
            && comboDTO.getStartDate().isAfter(comboDTO.getEndDate())) {
            throw new IllegalArgumentException("Ngày bắt đầu phải trước ngày kết thúc");
        }
    }
}
