package Group5_pizza.Pizza_GoGo.DTO;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ComboDTO {
    private Integer comboId;
    private String name;
    private BigDecimal discountPercent;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
    private List<ComboDetailDTO> comboDetails;
}
