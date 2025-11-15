package Group5_pizza.Pizza_GoGo.service.impl;

import Group5_pizza.Pizza_GoGo.DTO.VoucherDTO;
import Group5_pizza.Pizza_GoGo.model.Voucher;
import Group5_pizza.Pizza_GoGo.repository.VoucherRepository;
import Group5_pizza.Pizza_GoGo.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;

    @Override
    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    @Override
    public List<Voucher> getAllActiveVouchers() {
        return voucherRepository.findByIsActiveTrue();
    }

    @Override
    public Voucher getVoucherById(Integer voucherId) {
        if (voucherId == null) {
            throw new IllegalArgumentException("Voucher ID không được để trống");
        }
        return voucherRepository.findById(voucherId)
            .orElseThrow(() -> new RuntimeException("Voucher không tồn tại với ID: " + voucherId));
    }

    @Override
    public Voucher getVoucherByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã voucher không được để trống");
        }
        return voucherRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Voucher không tồn tại với mã: " + code));
    }

    @Override
    @Transactional
    public Voucher createVoucher(VoucherDTO voucherDTO) {
        validateVoucherDTO(voucherDTO);
        
        // Check if code already exists
        if (voucherRepository.findByCode(voucherDTO.getCode()).isPresent()) {
            throw new IllegalArgumentException("Mã voucher đã tồn tại: " + voucherDTO.getCode());
        }
        
        Voucher voucher = new Voucher();
        voucher.setCode(voucherDTO.getCode().trim().toUpperCase());
        voucher.setDiscountPercent(voucherDTO.getDiscountPercent());
        voucher.setDiscountAmount(voucherDTO.getDiscountAmount());
        voucher.setStartDate(voucherDTO.getStartDate());
        voucher.setEndDate(voucherDTO.getEndDate());
        voucher.setIsActive(voucherDTO.getIsActive() != null ? voucherDTO.getIsActive() : true);
        voucher.setCreatedAt(LocalDateTime.now());
        voucher.setUpdatedAt(LocalDateTime.now());
        
        return voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public Voucher updateVoucher(Integer voucherId, VoucherDTO voucherDTO) {
        validateVoucherDTO(voucherDTO);
        
        Voucher existingVoucher = getVoucherById(voucherId);
        
        // Check if code is being changed and if new code already exists
        if (!existingVoucher.getCode().equals(voucherDTO.getCode().trim().toUpperCase())) {
            if (voucherRepository.findByCode(voucherDTO.getCode().trim().toUpperCase()).isPresent()) {
                throw new IllegalArgumentException("Mã voucher đã tồn tại: " + voucherDTO.getCode());
            }
        }
        
        existingVoucher.setCode(voucherDTO.getCode().trim().toUpperCase());
        existingVoucher.setDiscountPercent(voucherDTO.getDiscountPercent());
        existingVoucher.setDiscountAmount(voucherDTO.getDiscountAmount());
        existingVoucher.setStartDate(voucherDTO.getStartDate());
        existingVoucher.setEndDate(voucherDTO.getEndDate());
        if (voucherDTO.getIsActive() != null) {
            existingVoucher.setIsActive(voucherDTO.getIsActive());
        }
        existingVoucher.setUpdatedAt(LocalDateTime.now());
        
        return voucherRepository.save(existingVoucher);
    }

    @Override
    @Transactional
    public boolean deleteVoucher(Integer voucherId) {
        if (voucherId == null) {
            throw new IllegalArgumentException("Voucher ID không được để trống");
        }
        // Check if voucher exists
        getVoucherById(voucherId);
        voucherRepository.deleteById(voucherId);
        return true;
    }

    @Override
    @Transactional
    public boolean activateVoucher(Integer voucherId) {
        Voucher voucher = getVoucherById(voucherId);
        voucher.setIsActive(true);
        voucher.setUpdatedAt(LocalDateTime.now());
        voucherRepository.save(voucher);
        return true;
    }

    @Override
    @Transactional
    public boolean deactivateVoucher(Integer voucherId) {
        Voucher voucher = getVoucherById(voucherId);
        voucher.setIsActive(false);
        voucher.setUpdatedAt(LocalDateTime.now());
        voucherRepository.save(voucher);
        return true;
    }

    @Override
    public List<Voucher> searchVouchersByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return getAllVouchers();
        }
        return voucherRepository.findByCodeContainingIgnoreCase(code);
    }

    @Override
    public List<Voucher> getActiveVouchers() {
        LocalDateTime now = LocalDateTime.now();
        return voucherRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndIsActiveTrue(now, now);
    }

    @Override
    public VoucherDTO convertToDTO(Voucher voucher) {
        if (voucher == null) {
            return null;
        }
        
        VoucherDTO dto = new VoucherDTO();
        dto.setVoucherId(voucher.getVoucherId());
        dto.setCode(voucher.getCode());
        dto.setDiscountPercent(voucher.getDiscountPercent());
        dto.setDiscountAmount(voucher.getDiscountAmount());
        dto.setStartDate(voucher.getStartDate());
        dto.setEndDate(voucher.getEndDate());
        dto.setIsActive(voucher.getIsActive());
        dto.setCreatedAt(voucher.getCreatedAt());
        dto.setUpdatedAt(voucher.getUpdatedAt());
        
        return dto;
    }

    @Override
    public Voucher convertToEntity(VoucherDTO voucherDTO) {
        if (voucherDTO == null) {
            return null;
        }
        
        Voucher voucher = new Voucher();
        voucher.setVoucherId(voucherDTO.getVoucherId());
        voucher.setCode(voucherDTO.getCode());
        voucher.setDiscountPercent(voucherDTO.getDiscountPercent());
        voucher.setDiscountAmount(voucherDTO.getDiscountAmount());
        voucher.setStartDate(voucherDTO.getStartDate());
        voucher.setEndDate(voucherDTO.getEndDate());
        voucher.setIsActive(voucherDTO.getIsActive());
        voucher.setCreatedAt(voucherDTO.getCreatedAt());
        voucher.setUpdatedAt(voucherDTO.getUpdatedAt());
        
        return voucher;
    }

    @Override
    public boolean validateVoucher(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        
        try {
            Voucher voucher = getVoucherByCode(code.trim().toUpperCase());
            LocalDateTime now = LocalDateTime.now();
            
            // Check if voucher is active
            if (voucher.getIsActive() == null || !voucher.getIsActive()) {
                return false;
            }
            
            // Check if voucher is within valid date range
            if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
                return false;
            }
            
            if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
                return false;
            }
            
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private void validateVoucherDTO(VoucherDTO voucherDTO) {
        if (voucherDTO.getCode() == null || voucherDTO.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã voucher không được để trống");
        }
        
        if (voucherDTO.getCode().length() > 100) {
            throw new IllegalArgumentException("Mã voucher không được vượt quá 100 ký tự");
        }
        
        // At least one discount type must be provided
        if ((voucherDTO.getDiscountPercent() == null || voucherDTO.getDiscountPercent().compareTo(BigDecimal.ZERO) <= 0)
            && (voucherDTO.getDiscountAmount() == null || voucherDTO.getDiscountAmount().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("Phải có ít nhất một loại giảm giá (phần trăm hoặc số tiền)");
        }
        
        // Validate discount percent
        if (voucherDTO.getDiscountPercent() != null) {
            if (voucherDTO.getDiscountPercent().compareTo(BigDecimal.ZERO) < 0 
                || voucherDTO.getDiscountPercent().compareTo(new BigDecimal("100")) > 0) {
                throw new IllegalArgumentException("Phần trăm giảm giá phải từ 0 đến 100");
            }
        }
        
        // Validate discount amount
        if (voucherDTO.getDiscountAmount() != null && voucherDTO.getDiscountAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Số tiền giảm giá phải lớn hơn 0");
        }
        
        // Validate dates
        if (voucherDTO.getStartDate() != null && voucherDTO.getEndDate() != null 
            && voucherDTO.getStartDate().isAfter(voucherDTO.getEndDate())) {
            throw new IllegalArgumentException("Ngày bắt đầu phải trước ngày kết thúc");
        }
    }
}

