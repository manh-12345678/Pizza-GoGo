package Group5_pizza.Pizza_GoGo.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import Group5_pizza.Pizza_GoGo.model.Topping;
import Group5_pizza.Pizza_GoGo.repository.ToppingRepository;
import Group5_pizza.Pizza_GoGo.service.ToppingService;

@Service
public class ToppingServiceImpl implements ToppingService {

    private final ToppingRepository repository;

    public ToppingServiceImpl(ToppingRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Topping> getAllToppings() {
        return repository.findAll();
    }

    @Override
    public List<Topping> getAllAvailableToppings() {
        return repository.findByIsDeletedFalse();
    }

    @Override
    public Topping getToppingById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Topping saveTopping(Topping topping) {
        return repository.save(topping);
    }

    @Override
    public void deleteTopping(Integer id) {
        repository.deleteById(id);
    }

    @Override
    public List<Topping> searchToppings(String name) {
        if (name == null || name.isEmpty()) {
            return repository.findAll();
        }
        return repository.findByNameContainingIgnoreCase(name);
    }
}
