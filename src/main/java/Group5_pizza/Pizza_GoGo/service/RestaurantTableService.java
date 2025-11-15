// package Group5_pizza.Pizza_GoGo.service;
// RestaurantTableService.java
package Group5_pizza.Pizza_GoGo.service;
import Group5_pizza.Pizza_GoGo.model.RestaurantTable;
import java.util.List;

public interface RestaurantTableService {
    List<RestaurantTable> getAllTables();
    RestaurantTable getTableById(Integer id);
    RestaurantTable saveTable(RestaurantTable table, String baseUrl);
    void deleteTable(Integer id);
    RestaurantTable updateTableStatus(Integer tableId, String status);
}