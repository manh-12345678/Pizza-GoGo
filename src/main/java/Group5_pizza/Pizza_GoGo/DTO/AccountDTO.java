package Group5_pizza.Pizza_GoGo.DTO;

import Group5_pizza.Pizza_GoGo.model.Account;
import lombok.Data;

/**
 * DTO (Data Transfer Object) cho Account.
 * Dùng để hiển thị dữ liệu ra view và nhận dữ liệu từ form,
 * tránh lộ các trường nhạy cảm như passwordHash.
 */
@Data
public class AccountDTO {
    private Integer userId;
    private String username;
    private String fullName;
    private String email;
    private Integer roleId; // Dùng cho form
    private String roleName; // Dùng để hiển thị
    private Boolean isDeleted;
    private Boolean isConfirmed;

    // Trường này CHỈ dùng khi TẠO MỚI, không bao giờ được đọc từ DB ra
    private String password;

    /**
     * Hàm helper để chuyển đổi từ Entity Account sang AccountDTO
     */
    public static AccountDTO fromEntity(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setUserId(account.getUserId());
        dto.setUsername(account.getUsername());
        dto.setFullName(account.getFullName());
        dto.setEmail(account.getEmail());
        dto.setIsDeleted(account.getIsDeleted());
        dto.setIsConfirmed(account.getIsConfirmed());

        if (account.getRole() != null) {
            dto.setRoleId(account.getRole().getRoleId());
            dto.setRoleName(account.getRole().getRoleName());
        }
        return dto;
    }
}