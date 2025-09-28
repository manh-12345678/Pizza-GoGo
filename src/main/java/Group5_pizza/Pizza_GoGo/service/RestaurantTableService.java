package Group5_pizza.Pizza_GoGo.service;

import Group5_pizza.Pizza_GoGo.model.RestaurantTable;
import java.util.List;

public interface RestaurantTableService {
    List<RestaurantTable> getAllTables();
    RestaurantTable getTableById(Integer id);
    RestaurantTable saveTable(RestaurantTable table);
    void deleteTable(Integer id);
}
