package Group5_pizza.Pizza_GoGo.service;

import java.util.List;

import Group5_pizza.Pizza_GoGo.model.RestaurantTable;

public interface RestaurantTableService {
    List<RestaurantTable> getAllTables();

    RestaurantTable getTableById(Integer id);

    // RestaurantTable saveTable(RestaurantTable table);

    void deleteTable(Integer id);

    RestaurantTable saveTable(RestaurantTable table, String baseUrl);
}
