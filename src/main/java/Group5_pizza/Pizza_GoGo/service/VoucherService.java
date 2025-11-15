package Group5_pizza.Pizza_GoGo.service;

import Group5_pizza.Pizza_GoGo.DTO.VoucherDTO;
import Group5_pizza.Pizza_GoGo.model.Voucher;

import java.util.List;

public interface VoucherService {
    
    List<Voucher> getAllVouchers();
    
    List<Voucher> getAllActiveVouchers();
    
    Voucher getVoucherById(Integer voucherId);
    
    Voucher getVoucherByCode(String code);
    
    Voucher createVoucher(VoucherDTO voucherDTO);
    
    Voucher updateVoucher(Integer voucherId, VoucherDTO voucherDTO);
    
    boolean deleteVoucher(Integer voucherId);
    
    boolean activateVoucher(Integer voucherId);
    
    boolean deactivateVoucher(Integer voucherId);
    
    List<Voucher> searchVouchersByCode(String code);
    
    List<Voucher> getActiveVouchers();
    
    VoucherDTO convertToDTO(Voucher voucher);
    
    Voucher convertToEntity(VoucherDTO voucherDTO);
    
    boolean validateVoucher(String code);
}

