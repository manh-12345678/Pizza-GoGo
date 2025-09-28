package Group5_pizza.Pizza_GoGo.service.impl;

import Group5_pizza.Pizza_GoGo.model.RestaurantTable;
import Group5_pizza.Pizza_GoGo.repository.RestaurantTableRepository;
import Group5_pizza.Pizza_GoGo.service.RestaurantTableService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestaurantTableServiceImpl implements RestaurantTableService {
    private final RestaurantTableRepository repository;

    public RestaurantTableServiceImpl(RestaurantTableRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<RestaurantTable> getAllTables() {
        return repository.findAll();
    }

    @Override
    public RestaurantTable getTableById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public RestaurantTable saveTable(RestaurantTable table) {
        return repository.save(table);
    }

    @Override
    public void deleteTable(Integer id) {
        repository.deleteById(id);
    }
}
