// package Group5_pizza.Pizza_GoGo.DTO;
// TableDTO.java
package Group5_pizza.Pizza_GoGo.DTO;
import lombok.Data;
@Data
public class TableDTO {
    private Integer tableId;
    private String tableName;
    public TableDTO(Integer tableId, String tableName) {
        this.tableId = tableId;
        this.tableName = tableName;
    }
}